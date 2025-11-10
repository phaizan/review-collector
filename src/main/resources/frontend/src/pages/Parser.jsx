import React, {useEffect, useState} from 'react';
import axios from "axios";
import  '../styles/modal.css';


const Parser = () => {
    const [isParsing, setIsParsing] = useState(false);
    const [parseResults, setParseResults] = useState([]);
    const [templates, setTemplates] = useState([""]);
    const [template, setTemplate] = useState("");
    const [modal, setModal] = useState(false);


    const parse = async (e) => {
        try {
            setIsParsing(true);
            const linksArr = e.target.links.value.split('\n').map(l => l.trim()).filter(l => l);
            const response = await axios.post('http://localhost:8080/api/parser/run', linksArr)
            console.log(response.data);
            setIsParsing(false);
            setParseResults(response.data)
        } catch (e) {
            console.error(e);
        }
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
            </table>)
    }



    const setDate = (date) => {
        return date ? new Date(date).toLocaleString('ru-RU', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : '—'
    }

    const handleTemplateChange = (e) => {
        if (e === "create")
            setModal(true);
        else {
            console.log("Загружаю шаблон...")
        }
    }

    const createTemplate = async (e) => {

        const links = e.target.tmpLinks.value.split('\n').map(l => l.trim()).filter(l => l);
        const name = e.target.name.value;
        try {
            const response = await axios.post(
                'http://localhost:8080/api/template/save',
                {
                    name: name,
                    links: links
                },{ headers: {Authorization: `Bearer ${localStorage.getItem('token')}`}}
            );
            console.log(response);
        }
        catch (e) {
            console.error('Ошибка при создании шаблона', e);
        }
    }

    useEffect(() => {

        const getRoles = async () => {
            try {
                const response = await axios.get('http://localhost:8080/api/template/get');
                setTemplates(response.data);
            }
            catch (e) {
                console.error('Ошибка при загрузке шаблонов', e)
                return [];
            }
        }
        getRoles();
        },[])

    return (
        <div >
            <form onSubmit={(e) => {
                e.preventDefault();
                parse(e);
            }}>
                <div>Введите список ссылок</div>
                <textarea
                rows={10}
                cols={50}
                name="links"
                placeholder="Список ссылок"/>
                <button type="submit">parse</button>
                <select
                    value={template}
                    onChange={e => handleTemplateChange(e.target.value)}>
                    <option value="">Выберите шаблон</option>
                    {templates.map(t => (
                        <option key={t.id} value={t.id}>{t.name}</option>
                    ))}
                    <option value="5">Крутой шаблон</option>

                    <option value="create">Создать новый</option>

                </select>


            </form>
            {isParsing && <div>
                Пожалуйста подождите...
            </div>}
            {parseResults.length > 0 && showParseResults()}
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



            <div>

            </div>
        </div>
    )


}


export default Parser;