import React, {useState} from 'react';
import { useAuth } from "../context/AuthContext";
import Parser from "./Parser";
import {useNavigate} from "react-router-dom";
import History from "./History";
import  '../styles/modal.css';

const Main = () => {

    const {} = useAuth();
    const navigate = useNavigate();
    const [show, setShow] = useState(0);
    return (
        <>
            {show % 10 === 0 &&
                <>
                    <button className="btn" onClick={() => setShow(1)}>Показать историю</button>
                    <Parser/>
                </>
            }
            {show % 10 === 1 &&
                <>
                    <button className="btn" onClick={() => setShow(0)}>Показать парсер</button>
                    <History/>
                </>
            }

        </>
    )
}

export default Main;
