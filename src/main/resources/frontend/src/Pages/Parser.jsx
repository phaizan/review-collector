import React, { useState } from 'react';
import axios from "axios";


const Parser = () => {
    const [links, setLinks] = useState("");


    const parse = async () => {
        try {
            const linksArr = links.split('\n').map(l => l.trim()).filter(l => l);
            console.log(linksArr)
            const response = await axios.post('http://localhost:8080/api/parser/run', linksArr)
            console.log(response);
            console.log(response.data);
        } catch (e) {
            console.error(e);
        }


    }

    return (
        <div>
            <div>Введите список ссылок</div>
            <form onSubmit={(e) => {
                e.preventDefault();
                parse();
            }}>
                <textarea value={links}
                onChange={(e) => setLinks(e.target.value)}
                rows={10}
                cols={50}/>
                <button type="submit">parse</button>
            </form>


            Пиздец))
        </div>
    )


}


export default Parser;