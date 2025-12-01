import React, {useEffect, useState} from 'react';
import axios from "axios";
import {useAuth} from "../context/AuthContext";

const History = () => {

    const [isLoading, setIsLoading] = useState(false);
    const [historyResults, setHistoryResults] = useState([]);
    const { getToken } = useAuth();

    const getHistory = async () => {
        try {
            setIsLoading(true);
            const response = await axios.get(
                '/api/history/getAll',
                { headers: {Authorization: `Bearer ${getToken()}`}}
            );
            setIsLoading(false);
            setHistoryResults(response.data);
        } catch (e) {
            console.error(e);
        }
    }

    const setDate = (date) => {
        return date ? new Date(date).toLocaleString('ru-RU', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : '—'
    }

    const showHistoryResults = () => {
        return (
            <table className="table">

                <thead>
                <tr>
                    <th>№</th>
                    <th>Источник</th>
                    <th>Рейтинг</th>
                    <th>Количество отзывов</th>
                    <th>Дата</th>
                    <th>Ссылка</th>
                </tr>
                </thead>

                <tbody>
                {historyResults.map((d, i) => (d &&
                    <tr>
                        <td>{i + 1}</td>
                        <td>{d.source}</td>
                        <td>{d.rating}</td>
                        <td>{d.reviewsCount}</td>
                        <td>{setDate(d.parsedAt)}</td>
                        <td>{d.url}</td>
                    </tr>
                ))}
                </tbody>
            </table>)
    }

    useEffect(() => {
        getHistory();
    }, []);

    return (
        <div>
            {isLoading && <div>Загрузка...</div>}
            {historyResults.length > 0 && showHistoryResults()}
            {historyResults.length === 0 &&
                <div>Пока не было ни одного запроса</div>}
        </div>
    )

}

export default History;