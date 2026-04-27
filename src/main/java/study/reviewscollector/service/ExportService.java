package study.reviewscollector.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import study.reviewscollector.dto.CardDTO;
import study.reviewscollector.dto.HistoryDTO;
import study.reviewscollector.dto.SnapshotDTO;
import org.apache.poi.ss.util.RegionUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final HistoryService historyService;

    public byte[] exportHistory(HistoryDTO request, Authentication authentication) throws IOException {
        List<CardDTO> cards = historyService.getHistory(request, authentication);
        List<LocalDate> dateColumns = cards.stream()
                .flatMap(card -> card.getSnapshots().stream())
                .map(SnapshotDTO::getParsedAt)
                .distinct()
                .sorted()
                .toList();
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            generateTable(workbook, cards, dateColumns);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public void generateTable(Workbook workbook, List<CardDTO> cards, List<LocalDate> dateColumns) {

        Sheet sheet = workbook.createSheet("Таблица");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle subHeaderStyle = createSubHeaderStyle(workbook);
        CellStyle textStyle = createTextStyle(workbook);
        CellStyle textStyleLeft = createTextStyleLeft(workbook, textStyle);
        CellStyle hyperlinkStyle = createHyperlinkStyle(workbook);
        CellStyle positiveStyle = createPositiveStyle(workbook);
        CellStyle negativeStyle = createNegativeStyle(workbook);
        CellStyle headerStyleRight = cloneWithRightBorder(workbook, headerStyle, BorderStyle.MEDIUM);
        CellStyle subHeaderStyleRight = cloneWithRightBorder(workbook, subHeaderStyle, BorderStyle.MEDIUM);
        CellStyle textStyleRight = cloneWithRightBorder(workbook, textStyle, BorderStyle.MEDIUM);
        CellStyle positiveStyleRight = cloneWithRightBorder(workbook, positiveStyle, BorderStyle.MEDIUM);
        CellStyle negativeStyleRight = cloneWithRightBorder(workbook, negativeStyle, BorderStyle.MEDIUM);

        int rowIndex = 0;

        Row headerRow1 = sheet.createRow(rowIndex++);
        Row headerRow2 = sheet.createRow(rowIndex++);

        CellRangeAddress sourceHeaderRegion = new CellRangeAddress(0, 1, 0, 0);
        CellRangeAddress nameHeaderRegion = new CellRangeAddress(0, 1, 1, 1);

        sheet.addMergedRegion(sourceHeaderRegion);
        sheet.addMergedRegion(nameHeaderRegion);

        createCell(headerRow1, 0, "Площадка", headerStyleRight);
        createCell(headerRow1, 1, "Название", headerStyleRight);

        applyMergedRegionRightBorder(sheet, sourceHeaderRegion);
        applyMergedRegionRightBorder(sheet, nameHeaderRegion);

        int colIndex = 2;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (LocalDate date : dateColumns) {
            createCell(headerRow1, colIndex, "Кол-во отзывов", headerStyle);
            createCell(headerRow1, colIndex + 1, "Рейтинг", headerStyleRight);

            CellRangeAddress dateRegion = new CellRangeAddress(1, 1, colIndex, colIndex + 1);
            sheet.addMergedRegion(dateRegion);
            createCell(headerRow2, colIndex, date.format(dateFormatter), subHeaderStyleRight);
            applyMergedRegionRightBorder(sheet, dateRegion);

            colIndex += 2;
        }

        CellRangeAddress reviewsDeltaRegion = new CellRangeAddress(0, 1, colIndex, colIndex);
        CellRangeAddress ratingDeltaRegion = new CellRangeAddress(0, 1, colIndex + 1, colIndex + 1);

        createCell(headerRow1, colIndex, "Изм. кол-ва отзывов", headerStyle);
        createCell(headerRow1, colIndex + 1, "Изм. рейтинга", headerStyleRight);

        sheet.addMergedRegion(reviewsDeltaRegion);
        sheet.addMergedRegion(ratingDeltaRegion);

        applyMergedRegionRightBorder(sheet, reviewsDeltaRegion);
        applyMergedRegionRightBorder(sheet, ratingDeltaRegion);

        int dataStartRow = rowIndex;

        for (CardDTO card : cards) {
            Row row = sheet.createRow(rowIndex++);

            createCell(row, 0, card.getSource(), textStyleLeft);
            createHyperlinkCell(row, 1, card.getName(), card.getUrl(), hyperlinkStyle, workbook);

            List<SnapshotDTO> snapshots = card.getSnapshots();

            SnapshotDTO firstSnapshot = snapshots.isEmpty() ? null : snapshots.get(0);
            SnapshotDTO previousSnapshot = null;

            Map<LocalDate, SnapshotDTO> snapshotMap = snapshots.stream()
                    .collect(Collectors.toMap(
                            SnapshotDTO::getParsedAt,
                            s -> s,
                            (a, b) -> b,
                            LinkedHashMap::new
                    ));

            colIndex = 2;

            for (LocalDate date : dateColumns) {
                SnapshotDTO snapshot = snapshotMap.get(date);

                if (snapshot != null) {

                    CellStyle reviewsStyle = getTrendStyle(
                            previousSnapshot == null ? null : previousSnapshot.getReviewsCount(),
                            snapshot.getReviewsCount(),
                            textStyle,
                            positiveStyle,
                            negativeStyle
                    );

                    CellStyle ratingStyle = getTrendStyle(
                            previousSnapshot == null ? null : previousSnapshot.getRating(),
                            snapshot.getRating(),
                            textStyleRight,
                            positiveStyleRight,
                            negativeStyleRight
                    );

                    createCell(row, colIndex, snapshot.getReviewsCount(), reviewsStyle);
                    createCell(row, colIndex + 1, snapshot.getRating(), ratingStyle);

                    previousSnapshot = snapshot;

                } else {
                    createCell(row, colIndex, "—", textStyle);
                    createCell(row, colIndex + 1, "—", textStyleRight);
                }

                colIndex += 2;
            }

            if (firstSnapshot != null && previousSnapshot != null) {

                int reviewsDelta = previousSnapshot.getReviewsCount() - firstSnapshot.getReviewsCount();
                double ratingDelta = roundToOneDecimal(previousSnapshot.getRating() - firstSnapshot.getRating());

                CellStyle reviewsStyle = getTrendStyle(
                        0,
                        reviewsDelta,
                        textStyle,
                        positiveStyle,
                        negativeStyle
                );

                CellStyle ratingStyle = getTrendStyle(
                        0.0,
                        ratingDelta,
                        textStyleRight,
                        positiveStyleRight,
                        negativeStyleRight
                );

                createCell(row, colIndex, reviewsDelta, reviewsStyle);
                createCell(row, colIndex + 1, ratingDelta, ratingStyle);
            } else {
                createCell(row, colIndex, "—", textStyle);
                createCell(row, colIndex + 1, "—", textStyleRight);
            }
        }

        mergeSourceCells(sheet, cards, dataStartRow, 0, 3 + dateColumns.size() * 2, workbook);

        applyBottomBorderToRow(headerRow2, 3 + dateColumns.size() * 2, workbook);

        int lastColumn = 3 + dateColumns.size() * 2;

        for (int i = 0; i <= lastColumn; i++) {
            sheet.autoSizeColumn(i, true);
        }

        int reviewsDeltaCol = 2 + dateColumns.size() * 2;
        int ratingDeltaCol = reviewsDeltaCol + 1;

        sheet.setColumnWidth(reviewsDeltaCol, Math.max(sheet.getColumnWidth(reviewsDeltaCol), 5000));
        sheet.setColumnWidth(ratingDeltaCol, Math.max(sheet.getColumnWidth(ratingDeltaCol), 3500));


    }

    private void applyMergedRegionRightBorder(Sheet sheet, CellRangeAddress region) {
        RegionUtil.setBorderRight(BorderStyle.MEDIUM, region, sheet);
        RegionUtil.setRightBorderColor(IndexedColors.BLACK.getIndex(), region, sheet);
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createCell(Row row, int col, int value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createCell(Row row, int col, double value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private void createHyperlinkCell(Row row, int col, String text, String url, CellStyle style, Workbook workbook) {
        Cell cell = row.createCell(col);

        cell.setCellValue(text);

        CreationHelper creationHelper = workbook.getCreationHelper();
        Hyperlink hyperlink = creationHelper.createHyperlink(HyperlinkType.URL);
        hyperlink.setAddress(url);
        cell.setHyperlink(hyperlink);

        cell.setCellStyle(style);
    }

    private CellStyle createHyperlinkStyle(Workbook workbook) {
        CellStyle style = createTextStyle(workbook);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setBorderRight(BorderStyle.MEDIUM);
        Font font = workbook.createFont();
        font.setUnderline(Font.U_SINGLE);
        font.setColor(IndexedColors.BLUE.getIndex());
        style.setFont(font);

        return style;
    }

    private CellStyle createSubHeaderStyle(Workbook workbook) {
        CellStyle style = createHeaderStyle(workbook);
        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createTextStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createTextStyleLeft(Workbook workbook, CellStyle baseStyle) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(baseStyle);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setBorderRight(BorderStyle.MEDIUM);
        return style;
    }

    private void applyBottomBorderToRow(Row row, int lastColumn, Workbook workbook) {
        if (row == null) return;

        for (int i = 0; i <= lastColumn; i++) {
            Cell cell = row.getCell(i);
            if (cell == null) {
                cell = row.createCell(i);
            }

            CellStyle newStyle = workbook.createCellStyle();
            if (cell.getCellStyle() != null) {
                newStyle.cloneStyleFrom(cell.getCellStyle());
            }
            newStyle.setBorderBottom(BorderStyle.MEDIUM);
            cell.setCellStyle(newStyle);
        }
    }

    private void mergeSourceCells(Sheet sheet, List<CardDTO> cards, int startRow, int sourceColIndex, int lastColumn, Workbook workbook) {
        if (cards.isEmpty()) {
            return;
        }

        int groupStart = startRow;
        String currentSource = cards.get(0).getSource();

        for (int i = 1; i < cards.size(); i++) {
            String nextSource = cards.get(i).getSource();

            if (!Objects.equals(currentSource, nextSource)) {
                int groupEnd = startRow + i - 1;

                if (groupEnd > groupStart) {
                    CellRangeAddress mergedRegion = new CellRangeAddress(groupStart, groupEnd, sourceColIndex, sourceColIndex);
                    sheet.addMergedRegion(mergedRegion);
                    applyMergedRegionRightBorder(sheet, mergedRegion);
                }

                applyBottomBorderToRow(sheet.getRow(groupEnd), lastColumn, workbook);

                groupStart = startRow + i;
                currentSource = nextSource;
            }
        }

        int lastRow = startRow + cards.size() - 1;
        if (lastRow > groupStart) {
            CellRangeAddress mergedRegion = new CellRangeAddress(groupStart, lastRow, sourceColIndex, sourceColIndex);
            sheet.addMergedRegion(mergedRegion);
            applyMergedRegionRightBorder(sheet, mergedRegion);
        }

        applyBottomBorderToRow(sheet.getRow(lastRow), lastColumn, workbook);
    }



    private <T extends Number & Comparable<T>> CellStyle getTrendStyle(
            T previousValue,
            T currentValue,
            CellStyle defaultStyle,
            CellStyle positiveStyle,
            CellStyle negativeStyle
    ) {
        if (previousValue == null || currentValue == null) {
            return defaultStyle;
        }

        if (currentValue.compareTo(previousValue) > 0) {
            return positiveStyle;
        }

        if (currentValue.compareTo(previousValue) < 0) {
            return negativeStyle;
        }

        return defaultStyle;
    }


    private CellStyle createPositiveStyle(Workbook workbook) {
        CellStyle style = createTextStyle(workbook);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createNegativeStyle(Workbook workbook) {
        CellStyle style = createTextStyle(workbook);
        style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle cloneWithRightBorder(Workbook workbook, CellStyle baseStyle, BorderStyle borderStyle) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(baseStyle);
        style.setBorderRight(borderStyle);
        return style;
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
