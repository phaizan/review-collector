import React, {useEffect, useState} from 'react';
import '../styles/Login.css'
import '../styles/Styles.css'
import api from "../api/axios";
import { useNavigate } from "react-router-dom";
import { useAuth } from  '../context/AuthProvider'

const Auth = () => {

    const navigate = useNavigate();
    const { login, token } = useAuth();
    const [message, setMessage] = useState(null);
    const [newUser, setNewUser] = useState({ name: '', login: '', password: '', confirmPassword: ''});
    const INIT_USER = { name: '', login: '', password: '', confirmPassword: '' };
    const [showLoginFrom, setShowLoginFrom] = useState(true);
    const [loginRequest, setLoginRequest] = useState({login: '', password: ''});
    const INIT_LOGIN = { login: '', password: '' };
    const loginFields= [
        {type: 'text', name: 'login', placeholder: "Логин"},
        {type: 'password', name: 'password', placeholder: "Пароль"},
    ]
    const registerFields= [
        {type: 'text', name: 'name', placeholder: "Имя"},
        {type: 'text', name: 'login', placeholder: "Логин"},
        {type: 'password', name: 'password', placeholder: "Пароль"},
        {type: 'password', name: 'confirmPassword', placeholder: "Повторите пароль"},
    ]

    const handleLogin = async (e) => {
        e.preventDefault();

        if (!loginRequest.login.trim()) {
            return;
        }
        try {
            const response = await api.post(
                '/user/login',
                loginRequest,
            );
            const token = response.data;
            login(token);
            navigate('/main/parser');
        }
        catch (e) {
            console.error(e);
            setMessage(e?.response?.data || "Ошибка при авторизации");
        }
    }

    const registr = async () => {
        try {
            if (newUser.password !== newUser.confirmPassword) {
                setMessage("Пароли не совпадают");
                return;
            }
            const response = await api.post('/user/registration', newUser);
            setMessage(`Пользователь ${response.data} зарегистрирован`);
            setShowLoginFrom(true);
        }
        catch (e) {
            setMessage(e?.response?.data || "Ошибка при регистрации");
        }
    }

    const handleLoginChange = (field, value) => {
        setLoginRequest({...loginRequest, [field]: value});
    }

    const handleNewUserChange = (field, value) => {
        setNewUser(prev => ({...prev, [field]: value}))
    }

    useEffect(() => {
        if (token)
            navigate('/main/parser');
    }, []);

    return (
        <div className="login">
            <div className="form-wrapper">
                <div className="form-box">
                    {showLoginFrom &&
                        <>
                            <h2>Авторизация</h2>
                            {message && (
                                <div className="message">
                                    <span>{message}</span>
                                </div>
                            )}
                            <form onSubmit={handleLogin}>
                                {loginFields.map(f => {
                                    return (
                                        <input
                                            type={f.type}
                                            required
                                            value={loginRequest[f.name]}
                                            onChange={e => handleLoginChange(f.name, e.target.value)}
                                            placeholder={f.placeholder}
                                        />
                                    )
                                })
                                }
                                <button className="btn" type="submit">Войти</button>
                                <p>
                                    Нет аккаунта? <a onClick={() => {
                                        setLoginRequest(INIT_LOGIN);
                                        setMessage(null);
                                        setShowLoginFrom(false)
                                }}>Зарегистрироваться</a>
                                </p>
                            </form>
                        </>
                    }
                    {!showLoginFrom &&
                        <>
                            <h2>Регистрация</h2>
                            <div className="message">{message}</div>
                            <form onSubmit={(e) => {
                                e.preventDefault();
                                registr();
                            }}>
                                {registerFields.map(f => {
                                    return (
                                        <input
                                            type={f.type}
                                            required
                                            value={newUser[f.name]}
                                            onChange={e => handleNewUserChange(f.name, e.target.value)}
                                            placeholder={f.placeholder}
                                        />
                                    )})
                                }
                                <button className="btn" type="submit">Зарегистрироваться</button>

                                <p>
                                    Уже есть аккаунт? <a onClick={() => {
                                        setNewUser(INIT_USER);
                                        setMessage(null);
                                        setShowLoginFrom(true)
                                }}>Войти</a>
                                </p>
                            </form>
                        </>
                    }
                </div>
            </div>
        </div>
    )
}

export default Auth;