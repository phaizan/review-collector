import React, { useEffect, useState} from 'react';
import api from "../api/axios";
import { getTemplates } from "./Set";
import "./../styles/Parser.css";
import {validateLinks} from "../util/validateLinks";
import {getSourceMeta} from "./History";

//TODO Сделать вывод резульатов также как в истории (группировать и сортировать по источнику)

export const handleTemplateChange = (e, templates, setLinks) => {
    e.preventDefault();
    const selectedTemplate = templates.find(t => t.id === Number(e.target.value)) ?? null;
    setLinks(selectedTemplate ? selectedTemplate.links.join('\n') : '');
}

const Parser = () => {
    const [isParsing, setIsParsing] = useState(false);
    const [message, setMessage] = useState("");
    const [parseResults, setParseResults] = useState([]);
    const [templates, setTemplates] = useState([]);
    const [links, setLinks] = useState("");
    const [badLinks, setBadLinks] = useState([]);

    useEffect(() => {
        const loadTemplates = async () => {
            try {
                const data = await getTemplates();
                setTemplates(data);
            } catch (e) {
                console.error(e);
                setMessage(e?.response?.data || e.message || "Ошибка при загрузке сетов");
            }
        };
        loadTemplates();
    }, []);

    const parse = async () => {
        if (links === "") {
            setMessage("Введите ссылки");
            return;
        }

        setParseResults([]);
        setBadLinks([]);

        const validation = validateLinks(links);

        if (!validation.valid) {
            console.log(validation);
            setMessage(validation.error);
            return;
        }

        if (validation.errors.length > 0) {
            setMessage(validation.errors.join('\n'));
            return;
        }

        try {
            setIsParsing(true);
            const response = await api.post(
                '/parser/run',
                validation.links);

            setParseResults(response.data.filter(d => d.errorMessage === null))
            setBadLinks(response.data.filter(d => d.errorMessage !== null))

        } catch (e) {
            setMessage(e?.response?.data || e.message || "Ошибка")
            //setMessage(e.message);
            console.error(e);
        }
        finally {
            setIsParsing(false);
        }
    }

    const putBadLinks = () => {
        setLinks(badLinks.map(b => b.url).join('\n'));
        setParseResults([]);
        setBadLinks([]);
    }

    const showParseResults = () => {

        const sortedResults = [...parseResults].sort((a, b) => {
            const sourceCompare = (a.source || "").localeCompare(b.source || "", "ru", {
                sensitivity: "base",
            });

            if (sourceCompare !== 0)
                return sourceCompare;

            return (a.name || "").localeCompare(b.name || "", "ru", {
                sensitivity: "base",
            });
        });

        const { counts, firstIndexes } = getSourceMeta(sortedResults);

        return (
            <div className="parser-results">
                <table className="table parser-table">
                    <thead>
                        <tr>
                            <th>Площадка</th>
                            <th>Название</th>
                            <th>Количество отзывов</th>
                            <th>Рейтинг</th>
                        </tr>
                    </thead>

                    <tbody>
                        {sortedResults.map((d, i) => {
                            const isFirstInGroup = firstIndexes[d.source] === i;
                            const isLastInSourceGroup =
                                i === sortedResults.length - 1 ||
                                sortedResults[i + 1].source !== d.source;
                            return (
                                <tr key={`${d.url}-${i}`} className={isLastInSourceGroup ? 'history-table__source-break' : ''}>
                                    {isFirstInGroup && (
                                        <td rowSpan={counts[d.source]} className="history-table__source-cell">
                                            {d.source}
                                        </td>
                                    )}
                                    <td className="history-table__fixed-end url">
                                        <a href={d.url}>
                                            {d.name}
                                        </a>
                                    </td>
                                    <td className="parser-table-reviews-count">{d.reviewsCount}</td>
                                    <td>{d.rating}</td>
                                </tr>

                            );
                        })}
                    </tbody>
                </table>
            </div>
        )
    }

    const showBadResults = () => {

        return (badLinks.length > 0 &&
            <div className="parser-errors">
                <div className="parser-errors__title">
                    Произошла ошибка в {badLinks.length} ссылк{badLinks.length === 1 ? "e" :  "ах"}
                </div>

                <ul className="parser-errors__list">
                    {badLinks.map((b, i) => (
                        <li key={`${b.url}-${i}`}>
                            {b.errorMessage}:{" "}
                            <a href={b.url} target="_blank" rel="noreferrer">
                                {b.url}
                            </a>
                        </li>))}


                </ul>

                <button className="btn btn-danger" onClick={putBadLinks}>Попробовать ещё раз</button>
            </div>
        )
    }



    //TODO вынести в отдельную страницу все что связано с сетами.
    //TODO переименовать template в сет
    //TODO После создания сета в поле для ввода ссылок появляется "create"

    return (
        <div>
            <h2>Введите список ссылок</h2>

            <form className="parser-card" onSubmit={(e) => {
                e.preventDefault();
                parse();
            }}>
                <textarea
                    className="parser-card__textarea"
                    rows={10}
                    onChange={e => setLinks(e.target.value)}
                    value={links}
                    disabled={isParsing}
                    placeholder="Список ссылок"/>
                <div className="parser-card__controls">
                    <button className="parser-card__submit" type="submit" disabled={isParsing}>Начать парсинг</button>
                    <select
                        className="parser-card__select"
                        disabled={isParsing}
                        defaultValue=""
                        onChange={(e) => handleTemplateChange(e, templates, setLinks)}
                    >
                        <option value="" disabled>Выберите сет</option>
                        {templates.map((t) => (
                            <option key={t.id} value={t.id}>
                                {t.name}
                            </option>
                        ))}
                    </select>
                </div>

            </form>
            {message && <div style={{ whiteSpace: 'pre-line' }}>{message}</div>}


            {isParsing && <div className="parser-loading">
                Пожалуйста, подождите...
            </div>}
            {parseResults.length > 0 && showParseResults()}
            {badLinks.length > 0 && showBadResults()}

        </div>
    )
}


export default Parser;