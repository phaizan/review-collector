import React, { useEffect, useState} from 'react';
import  '../styles/modal.css';
import {useAuth} from "../context/AuthProvider";
import api from "../api/axios";


const Parser = () => {
    const [isParsing, setIsParsing] = useState(false);
    const { getToken } = useAuth();
    const [message, setMessage] = useState("");
    const [parseResults, setParseResults] = useState([]);
    const [templates, setTemplates] = useState([""]);
    const [modal, setModal] = useState(false);
    const [links, setLinks] = useState("");
    const [badLinks, setBadLinks] = useState([]);

    const parse = async () => {
        if (links === "")
            return;

        setParseResults([]);
        setBadLinks([]);

        const linksArr = links
            .split('\n')
            .map(l => l.trim())
            .filter(l => l);
        console.log(links);
        console.log(linksArr);
        const invalidLinks = [];

        for (const link of linksArr) {
            try {
                const url = new URL(link);

                if (url.protocol !== "http:" && url.protocol !== "https:") {
                    invalidLinks.push(link);
                }

            } catch {
                invalidLinks.push(link);
            }
        }

        if (invalidLinks.length > 0) {
            setBadLinks(invalidLinks.map(l => ({ error: "Неверный формат ссылки", parsedData: { url: l }})));
            return;
        }

        try {
            setIsParsing(true);
            console.log(linksArr);
            const response = await api.post(
                '/parser/run',
                linksArr);

            console.log(linksArr);
            console.log(response.data);

            setIsParsing(false);
            setParseResults(response.data.filter(d => d.error === null))
            setBadLinks(response.data.filter(d => d.error !== null))
            /*setParseResults(response.data.filter(x => x !== null))

            setBadLinks(response.data
                .map((res, i) => res === null ? linksArr[i] : null)
                .filter(x => x !== null));*/

            console.log(badLinks);

        } catch (e) {
            console.error(e);
        }
    }

    const putBadLinks = () => {
        setLinks(badLinks.map(b => b.parsedData.url).join('\n'));
        setParseResults([]);
        setBadLinks([]);
    }

    const showParseResults = () => {
        return (
                <table className="table">

                    <thead>
                        <tr>
                            <th>№</th>
                            <th>Название</th>
                            <th>Источник</th>
                            <th>Рейтинг</th>
                            <th>Количество отзывов</th>
                            <th>Ссылка</th>
                        </tr>
                    </thead>

                    <tbody>
                        {parseResults.map((d, i) => (d &&
                            <tr>
                                <td>{i + 1}</td>
                                <td>{d.parsedData.name}</td>
                                <td>{d.parsedData.source}</td>
                                <td>{d.parsedData.rating}</td>
                                <td>{d.parsedData.reviewsCount}</td>
                                <td className="url"><a href={d.parsedData.url}>{d.parsedData.url}</a></td>
                            </tr>
                        ))}
                    </tbody>
                </table>
        )
    }

    const showBadResults = () => {

        console.log(badLinks);

        return (badLinks.length > 0 &&
            <div>Произошла ошибка в ссылк
                {badLinks.length === 1  && <>e</>}
                {badLinks.length > 1  && <>ах</>}
                :
                {badLinks.map((b, i) => (
                    <li>
                        {b.error}: <a href={b.parsedData.url}>{b.parsedData.url}</a>
                    </li>))}
                <button className="btn btn-danger" onClick={putBadLinks}>Попробовать ещё раз</button>
            </div>
        )
    }

    const handleTemplateChange = (e) => {
        if (e === "create")
            setModal(true);
        if (e === "")
            return;
        else {
            const links = e.split(',');
            setLinks(links.join('\n'));
        }
    }

    //TODO вынести в отдельную страницу все что связано с сетами.
    //TODO переименовать template в сет



    const createTemplate = async (e) => {
        setModal(false);
        const links = e.target.tmpLinks.value.split('\n').map(l => l.trim()).filter(l => l);
        const name = e.target.name.value;
        try {
            //TODO добавить проверку формата ссылок (вынести в отдельную функцию из parse)
            const response = await api.post(
                '/template/save',
                {
                    name: name,
                    links: links
                },{ headers: {Authorization: `Bearer ${getToken()}`}}
            );
            setTemplates(prev => [...prev, response.data]);
            setMessage(`Шаблон ${e.target.name.value} с ${links.length} ссылок создан`);
        }
        catch (e) {
            console.error('Ошибка при создании шаблона', e);
        }
    }

    const getTemplates = async () => {
        try {
            const response = await api.get('/template/get');
            setTemplates(response.data);
        }
        catch (e) {
            console.error('Ошибка при загрузке шаблонов', e)
            return [];
        }
    }

    useEffect(() => {
        getTemplates();
        },[])

    return (
        <div>
            {message}
            <form onSubmit={(e) => {
                e.preventDefault();
                parse();
            }}>
                <h2>Введите список ссылок</h2>
                <textarea
                rows={10}
                onChange={e => setLinks(e.target.value)}
                cols={50}
                value={links}
                disabled={isParsing}
                placeholder="Список ссылок"/>
                <div className="form-actions">
                    <button className="btn" type="submit" disabled={isParsing}>Начать парсинг</button>
                    <select
                        onChange={e => handleTemplateChange(e.target.value)}>
                        <option value="">Выберите сет</option>
                        {templates.map(t => (
                            <option key={t.id} value={t.links}>{t.name}</option>
                        ))}
                        <option value="create">Создать новый</option>
                    </select>
                </div>
            </form>
            {isParsing && <div>
                Пожалуйста подождите...
            </div>}
            {parseResults.length > 0 && showParseResults()}
            {badLinks.length > 0 && showBadResults()}
            {modal && (
                <div className="modal-overlay" onClick={() => setModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <form onSubmit={(e) => {
                            e.preventDefault();
                            createTemplate(e);
                        }}>
                            <h2>Создание шаблона</h2>

                            <input
                                className="input"
                                placeholder="Название"
                                name="name"/>

                            <textarea
                                placeholder="Список ссылок"
                                rows={10}
                                cols={50}
                                name="tmpLinks"
                                className="input"
                            />

                            <div className="form-actions">
                                <button onClick={() => setModal(false)}>Закрыть</button>
                                <button type="submit">Создать</button>
                            </div>
                        </form>

                    </div>
                </div>
            )}
        </div>
    )
}


export default Parser;