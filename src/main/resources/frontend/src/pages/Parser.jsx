import React, {use, useEffect, useState} from 'react';
import axios from "axios";
import  '../styles/modal.css';
import {useAuth} from "../context/AuthContext";


const Parser = () => {
    const [isParsing, setIsParsing] = useState(false);
    const { getToken } = useAuth();
    const [message, setMessage] = useState("");
    const [parseResults, setParseResults] = useState([]);
    const [templates, setTemplates] = useState([""]);
    const [modal, setModal] = useState(false);
    const [links, setLinks] = useState("");
    const [badLinks, setBadLinks] = useState([]);

    const parse = async (badLinks = null) => {
        if (links === "")
            return;
        try {
            setParseResults([]);
            setIsParsing(true);
            const linksArr = (badLinks ?? links).split('\n').map(l => l.trim()).filter(l => l);
            console.log(linksArr);
            const response = await axios.post(
                '/api/parser/run',
                linksArr,
                { headers: {Authorization: `Bearer ${getToken()}`}}
            );

            console.log(linksArr);
            console.log(response.data);

            setIsParsing(false);
            setParseResults(response.data.filter(x => x !== null))

            setBadLinks(response.data
                .map((res, i) => res === null ? linksArr[i] : null)
                .filter(x => x !== null));

            console.log(badLinks);

        } catch (e) {
            console.error(e);
        }
    }

    const putBadLinks = () => {
        setLinks(badLinks.join('\n'));
        setParseResults([]);
        setBadLinks([]);
        parse(badLinks.join('\n'));
    }

    const showParseResults = () => {
        return (
                <table className="table">

                    <thead>
                        <tr>
                            <th>№</th>
                            <th>Источник</th>
                            <th>Ссылка</th>
                            <th>Рейтинг</th>
                            <th>Количество отзывов</th>
                        </tr>
                    </thead>

                    <tbody>
                        {parseResults.map((d, i) => (d &&
                            <tr>
                                <td>{i + 1}</td>
                                <td>{d.source}</td>
                                <td>{d.url}</td>
                                <td>{d.rating}</td>
                                <td>{d.reviewsCount}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
        )
    }

    const showBadResults = () => {
        return ( badLinks.length > 0 &&
            <div>Произошла ошибка в ссылке(-ах):
                {badLinks.map((b, i) => (
                    <li>
                        {b}
                    </li>))}
                <button onClick={putBadLinks}>Попробовать ещё раз</button>
            </div>
        )
    }

    const setDate = (date) => {
        return date ? new Date(date).toLocaleString('ru-RU', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : '—'
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

    const createTemplate = async (e) => {
        setModal(false);
        const links = e.target.tmpLinks.value.split('\n').map(l => l.trim()).filter(l => l);
        const name = e.target.name.value;
        setMessage(`Шаблон ${e.target.name.value} с ${links.length} ссылок создан`);
        try {
            const response = await axios.post(
                '/api/template/save',
                {
                    name: name,
                    links: links
                },{ headers: {Authorization: `Bearer ${getToken()}`}}
            );
            setTemplates(prev => [...prev, response.data]);
        }
        catch (e) {
            console.error('Ошибка при создании шаблона', e);
        }
    }

    const getTemplates = async () => {
        try {
            const response = await axios.get(
                '/api/template/get',
                { headers: {Authorization: `Bearer ${getToken()}`}});
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
                <div>Введите список ссылок</div>
                <textarea
                rows={10}
                onChange={e => setLinks(e.target.value)}
                cols={50}
                value={links}
                disabled={isParsing}
                placeholder="Список ссылок"/>
                <button type="submit" disabled={isParsing}>parse</button>
                <select
                    onChange={e => handleTemplateChange(e.target.value)}>
                    <option value="">Выберите шаблон</option>
                    {templates.map(t => (
                        <option key={t.id} value={t.links}>{t.name}</option>
                    ))}
                    <option value="create">Создать новый</option>

                </select>

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
                            placeholder="Название"
                            name="name"/>

                            <textarea
                                placeholder="Список ссылок"
                                rows={10}
                                cols={50}
                                name="tmpLinks"
                            />

                            <button onClick={() => setModal(false)}>Закрыть</button>

                            <button type="submit">Создать</button>
                        </form>

                    </div>
                </div>
            )}

        </div>
    )


}


export default Parser;