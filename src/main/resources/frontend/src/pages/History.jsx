import React, {useEffect, useState} from 'react';
import api from "../api/axios";
import '../styles/History.css'
import {getTemplates} from "./Set";
import {handleTemplateChange} from "./Parser";
import {validateLinks} from "../util/validateLinks";


const History = () => {

    const [isLoading, setIsLoading] = useState(false);
    const [historyResults, setHistoryResults] = useState([]);
    const [links, setLinks] = useState("");
    const [message, setMessage] = useState("");
    const [templates, setTemplates] = useState([]);
    const [show, setShow] = useState(false);
    const [request, SetRequest] = useState({
        links: null,
        dateFrom: null,
        dateTo: null,
    });

    useEffect(() => {
        const loadTemplates = async () => {
            try {
                const data = await getTemplates();
                setTemplates(data);
            } catch (e) {
                console.error(e);
                setMessage(e?.response?.data || e.message || "Ошибка при загрузке сетов");
            } finally {
                setIsLoading(false);
            }
        };

        loadTemplates();
    }, []);

    const normalizeDateKey = (date) => {
        if (!date) return '';
        const d = new Date(date);

        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        const hours = String(d.getHours()).padStart(2, '0');
        const minutes = String(d.getMinutes()).padStart(2, '0');

        return `${year}-${month}-${day} ${hours}:${minutes}`;
    };

    const formatDateLabel = (dateKey) => {
        if (!dateKey) return '—';
        const date = dateKey.split(' ')[0];
        const [year, month, day] = date.split('-');
        return `${day}.${month}.${year}`;
    };

    const dateColumns = [...new Set(
        historyResults.flatMap(card =>
            card.snapshots.map(s => normalizeDateKey(s.parsedAt))
        )
    )].sort();

    const loadHistory = async (e) => {

        e.preventDefault();
        setMessage("");
        setShow(false)

        const validation = validateLinks(links);

        if (!validation.valid) {
            setMessage(validation.error);
            return;
        }

        if (validation.errors.length > 0) {
            setMessage(validation.errors.join('\n'));
            return;
        }

        const payload = {
            ...request,
            links: validation.links
        };

        try {
            setIsLoading(true);
            console.log(payload);
            const response = await api.post('/history/get', payload);
            setHistoryResults(response.data);
            console.log(response);
            setShow(true);
        } catch (e) {
            console.error(e);
            setMessage(e?.response?.data || e.message || "Ошибка при загрузке истории");
        }
        finally {
            setIsLoading(false);
        }
    }

    const getReport = async (e) => {

        e.preventDefault();
        setMessage("");

        const validation = validateLinks(links);

        if (!validation.valid) {
            setMessage(validation.error);
            return;
        }

        if (validation.errors.length > 0) {
            setMessage(validation.errors.join('\n'));
            return;
        }

        const payload = {
            ...request,
            links: validation.links
        };

        try {
            setIsLoading(true);
            console.log(payload);
            const response = await api.post('/report/create', payload, {
                responseType: 'blob'
            });

            const blob = new Blob([response.data]);
            const downloadUrl = window.URL.createObjectURL(blob);

            const link = document.createElement('a');
            link.href = downloadUrl;

            let fileName = 'report.xlsx';

            link.setAttribute('download', fileName);

            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(downloadUrl);

        } catch (e) {
            console.error(e);
            setMessage(e?.response?.data || e.message || "Ошибка при загрузке истории");
        } finally {
            setIsLoading(false);
        }


    }

    const getTrendClass = (previousValue, currentValue) => {
        if (previousValue == null || currentValue == null) return '';
        if (currentValue > previousValue) return 'history-cell--positive';
        if (currentValue < previousValue) return 'history-cell--negative';
        return '';
    };

    const roundToOneDecimal = (value) => {
        return Math.round(value * 10) / 10;
    };

    const formatSignedInt = (value) => {
        return value > 0 ? `+${value}` : String(value);
    };

    const formatSignedDouble = (value) => {
        if (Math.abs(value) < 0.0000001) return '0.0';
        return value > 0 ? `+${value.toFixed(1)}` : value.toFixed(1);
    };

    const getPreparedCard = (card) => {
        const snapshots = [...card.snapshots].sort(
            (a, b) => new Date(a.parsedAt) - new Date(b.parsedAt)
        );

        const snapshotMap = {};
        let previousSnapshot = null;

        snapshots.forEach((snapshot) => {
            const key = normalizeDateKey(snapshot.parsedAt);

            snapshotMap[key] = {
                ...snapshot,
                reviewsClass: getTrendClass(
                    previousSnapshot?.reviewsCount,
                    snapshot.reviewsCount
                ),
                ratingClass: getTrendClass(
                    previousSnapshot?.rating,
                    snapshot.rating
                )
            };

            previousSnapshot = snapshot;
        });

        const firstSnapshot = snapshots[0] ?? null;
        const lastSnapshot = snapshots[snapshots.length - 1] ?? null;

        const reviewsDelta =
            firstSnapshot && lastSnapshot
                ? lastSnapshot.reviewsCount - firstSnapshot.reviewsCount
                : null;

        const ratingDelta =
            firstSnapshot && lastSnapshot
                ? roundToOneDecimal(lastSnapshot.rating - firstSnapshot.rating)
                : null;

        return {
            ...card,
            snapshotMap,
            reviewsDelta,
            ratingDelta,
            reviewsDeltaClass:
                reviewsDelta == null ? '' : getTrendClass(0, reviewsDelta),
            ratingDeltaClass:
                ratingDelta == null ? '' : getTrendClass(0, ratingDelta)
        };
    };

    const getSourceMeta = (cards) => {
        const counts = {};
        const firstIndexes = {};

        cards.forEach((card, index) => {
            counts[card.source] = (counts[card.source] || 0) + 1;
            if (firstIndexes[card.source] === undefined) {
                firstIndexes[card.source] = index;
            }
        });

        return { counts, firstIndexes };
    };

    const showHistoryResults = () => {
        const preparedCards = historyResults.map(getPreparedCard);
        const { counts, firstIndexes } = getSourceMeta(preparedCards);

        return (
            <table className="table history-table">
                <thead>
                <tr>
                    <th rowSpan={2}>Площадка</th>
                    <th rowSpan={2} className="history-table__fixed-end">Название</th>

                    {dateColumns.map((date, i) => (
                        <React.Fragment key={i}>
                            <th>Кол-во отзывов</th>
                            <th className="history-table__group-end">Рейтинг</th>
                        </React.Fragment>
                    ))}

                    <th rowSpan={2}>Изм. кол-ва отзывов</th>
                    <th rowSpan={2} className="history-table__group-end">Изм. рейтинга</th>
                </tr>

                <tr>
                    {dateColumns.map((date, i) => (
                        <th key={i} colSpan={2} className="history-table__group-end">
                            {formatDateLabel(date)}
                        </th>
                    ))}
                </tr>
                </thead>

                <tbody>
                {preparedCards.map((card, index) => {
                    const isFirstInSourceGroup = firstIndexes[card.source] === index;
                    const isLastInSourceGroup =
                        index === preparedCards.length - 1 ||
                        preparedCards[index + 1].source !== card.source;

                    return (
                        <tr
                            key={card.url}
                            className={isLastInSourceGroup ? 'history-table__source-break' : ''}
                        >
                            {isFirstInSourceGroup && (
                                <td
                                    rowSpan={counts[card.source]}
                                    className="history-table__source-cell"
                                >
                                    {card.source}
                                </td>
                            )}

                            <td className="history-table__fixed-end url">
                                <a href={card.url} target="_blank" rel="noreferrer">
                                    {card.name}
                                </a>
                            </td>

                            {dateColumns.map((dateKey, j) => {
                                const snapshot = card.snapshotMap[dateKey];

                                return (
                                    <React.Fragment key={j}>
                                        <td className={snapshot?.reviewsClass || ''}>
                                            {snapshot ? snapshot.reviewsCount : '—'}
                                        </td>
                                        <td className={`${snapshot?.ratingClass || ''} history-table__group-end`}>
                                            {snapshot ? snapshot.rating : '—'}
                                        </td>
                                    </React.Fragment>
                                );
                            })}

                            <td className={card.reviewsDeltaClass || ''}>
                                {card.reviewsDelta == null ? '—' : formatSignedInt(card.reviewsDelta)}
                            </td>
                            <td className={`${card.ratingDeltaClass || ''} history-table__group-end`}>
                                {card.ratingDelta == null ? '—' : formatSignedDouble(card.ratingDelta)}
                            </td>
                        </tr>
                    );
                })}
                </tbody>
            </table>
        );
    };



    return (
        <div>

            <section>
                <h2>Просмотр истории</h2>

                <form className="history-form">
                    <div className="history-form__left">
                        <div className="history-form__row history-form__row--select">
                            <label className="history-form__label history-form__label--select" htmlFor="set">
                                Выберите сет:
                            </label>

                            <select className="history-form__select" id="set" onChange={(e) => handleTemplateChange(e, templates, setLinks)}>
                                <option value="">Сет</option>
                                {templates.map(t => (
                                    <option key={t.id} value={t.id}>{t.name}</option>
                                ))}
                            </select>
                        </div>

                        <div className="history-form__row history-form__row--date">
                            <label className="history-form__label history-form__label--date" htmlFor="dateFrom">
                                Дата от:
                            </label>

                            <div className="history-form__date-control">
                                <input className="history-form__date-input" id="dateFrom" type="date"
                                       value={request.dateFrom}
                                       required
                                       min="2000-01-01"
                                       max="2100-12-31"
                                       onChange={(e) => SetRequest({...request, dateFrom: e.target.value})}
                                />
                            </div>
                        </div>

                        <div className="history-form__row history-form__row--date">
                            <label className="history-form__label history-form__label--date" htmlFor="dateTo">
                                Дата до:
                            </label>

                            <div className="history-form__date-control">
                                <input className="history-form__date-input" id="dateTo" type="date"
                                       value={request.dateTo}
                                       required
                                       min="2000-01-01"
                                       max="2100-12-31"
                                       onChange={(e) => SetRequest({...request, dateTo: e.target.value})}
                                />
                            </div>
                        </div>

                        <button className="history-form__submit" onClick={loadHistory}>
                            Загрузить
                        </button>
                        <button className="history-form__submit" onClick={getReport}>
                            Экспорт
                        </button>
                    </div>

                    <textarea
                        className="history-form__textarea"
                        placeholder="Введите список ссылок"
                        value={links}
                        onChange={e => setLinks(e.target.value)}
                    ></textarea>
                </form>

                {message && <div style={{ whiteSpace: 'pre-line' }}>{message}</div>}

            </section>

            {isLoading && <div>Загрузка...</div>}
            {!isLoading && show &&
                <div>
                    {historyResults.length > 0 && showHistoryResults()}
                    {historyResults.length === 0 && <div>Пока не было ни одного запроса</div>}
                </div>}
        </div>
    )

}

export default History;
