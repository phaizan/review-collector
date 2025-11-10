import React from 'react';
import { useAuth } from "../context/AuthContext";
import Parser from "./Parser";

const Main = () => {

    const {} = useAuth();
    return (
        <>
            <Parser/>
        </>
    )
}

export default Main;
