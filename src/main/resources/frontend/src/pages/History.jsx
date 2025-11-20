import React, {useEffect, useState} from 'react';
import axios from "axios";
import {useAuth} from "../context/AuthContext";

const History = () => {

    const [isLoading, setIsLoading] = useState(false);
    const [historyResults, setHistoryResults] = useState([]);
    const { getToken } = useAuth();

    //const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

    const getHistory = async () => {
        try {
            setIsLoading(true);
            const response = await axios.get(
                'http://localhost:8080/api/history/getAll',
                { headers: {Authorization: `Bearer ${getToken()}`}}
            );
            console.log(response.data);
            setIsLoading(false);
            setHistoryResults(response.data);
        } catch (e) {
            console.error(e);
        }
    }

    const showHistoryResults = () => {
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
                {historyResults.map((d, i) => (d &&
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