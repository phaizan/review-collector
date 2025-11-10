import React, { createContext, useState, useContext } from 'react';
import {useNavigate} from "react-router-dom";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {

    const USER_DATA_SESSION_KEY = "UserData";

    const [user, setUser] = useState(() => {
        const saved = localStorage.getItem(USER_DATA_SESSION_KEY);
        console.log("Пользователь", saved);
        return saved ? JSON.parse(saved) : null;
    });

    const navigate = useNavigate();

    const saveUser = (userData) => {
        setUser(userData);
        localStorage.setItem(USER_DATA_SESSION_KEY, JSON.stringify(userData));
    };

    const removeUser = () => {
        setUser(null);
        localStorage.removeItem(USER_DATA_SESSION_KEY);
        navigate("/");
    };

    return (
        <AuthContext.Provider value={{ user, saveUser, removeUser }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
