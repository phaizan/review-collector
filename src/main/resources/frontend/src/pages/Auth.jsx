import React, { useEffect, useState} from 'react';
import '../styles/Login.css'
import '../styles/Styles.css'
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const Auth = () => {

    const navigate = useNavigate();
    const { user, saveUser } = useAuth();
    const [message, setMessage] = useState(String);
    const [newUser, setNewUser] = useState({ name: '', login: '', password: ''});
    const [showLoginFrom, setShowLoginFrom] = useState(true);
    const [loginRequest, setLoginRequest] = useState({login: '', password: ''});
    const loginFields= [
        {type: 'text', name: 'login', placeholder: "Логин"},
        {type: 'password', name: 'password', placeholder: "Пароль"},
    ]
    const registrFields= [
        {type: 'text', name: 'name', placeholder: "Имя"},
        {type: 'text', name: 'login', placeholder: "Логин"},
        {type: 'password', name: 'password', placeholder: "Пароль"},

    ]

    useEffect(() => {
        if (user) {
            navigate("/main");
        }
    }, [user, navigate]);

    const login = async () => {
        if (!loginRequest.login.trim()) {
            return;
        }

        try {
            const response = await axios.post(
                'http://localhost:8080/api/user/login',
                loginRequest,
            );

            const loggedUser = response.data;
            const {token} = response.data;
            localStorage.setItem('token', token)
            console.log("Токен", token);
            saveUser(loggedUser);
            navigate('/main');
        }
        catch (e) {
            console.error(e);
            setMessage(e?.response?.data || "Ошибка при авторизации");
        }
    }

    const registr = async () => {
        try {
            const response = await axios.post('http://localhost:8080/api/user/registration', newUser);
            console.log(response);
            setMessage(`Пользователь ${response.data.name} зарегистрирован`);
            setShowLoginFrom(true);
        }
        catch (e) {
            setMessage(e?.response?.data || "Ошибка при регистрации");
        }
    }


    const handleLoginChange = (field, value) => {
        setLoginRequest(prev => ({...prev, [field]: value}))
    }

    const handleUserChange = (field, value) => {
        setNewUser(prev => ({...prev, [field]: value}))
    }

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
                            <form onSubmit={(e) => {
                                e.preventDefault();
                                login();
                            }}>
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
                                {registrFields.map(f => {
                                    return (
                                        <input
                                            type={f.type}
                                            required
                                            value={newUser[f.name]}
                                            onChange={e => handleUserChange(f.name, e.target.value)}
                                            placeholder={f.placeholder}
                                        />
                                    )})
                                }
                                <button className="btn" type="submit">Зарегистрироваться</button>

                                <p>
                                    Уже есть аккаунт? <a onClick={() => {
                                    setShowLoginFrom(true)
                                }}>Войти</a>
                                </p>
                            </form>
                        </>
                    }
                </div>
            </div>



            {/*<div className="login">
                <div className="form-wrapper">
                    <div className="form-box">
                        { !user && !user?.name &&
                            (<>
                                {showLoginFrom &&
                                    <>
                                        <h2>Авторизация</h2>
                                        <form onSubmit={(e) => {
                                            e.preventDefault();
                                            login();
                                        }}>
                                            <input
                                                type="text"
                                                required
                                                value={loginRequest.login}
                                                onChange={e => handleLoginChange("login", e.target.value)}
                                                placeholder="Логин"
                                            />

                                            <input
                                                type="password"
                                                required
                                                value={loginRequest.password}
                                                onChange={e => handleLoginChange("password", e.target.value)}
                                                placeholder="Пароль"
                                            />

                                            <button className="btn" type="submit">Войти</button>
                                            <p>
                                                Нет аккаунта? <a onClick={() => {
                                                resetMessage(setMessage, setMessageId);
                                                setShowRegistrationFrom(true);
                                                setShowLoginFrom(false)
                                            }}>Зарегистрироваться</a>
                                            </p>
                                        </form>
                                    </>
                                }
                                {showRegistrationFrom &&
                                    <>
                                        <h2>Регистрация</h2>
                                        <form onSubmit={(e) => {
                                            e.preventDefault();
                                            registr();
                                        }}>
                                            <input
                                                type="text"
                                                required
                                                value={newUser?.name}
                                                onChange={e => handleUserChange("name", e.target.value)}
                                                placeholder="Имя"
                                            />
                                            <input
                                                type="text"
                                                required
                                                value={newUser?.login}
                                                onChange={e => handleUserChange("login", e.target.value)}
                                                placeholder="Логин"
                                            />
                                            <input
                                                type="password"
                                                required
                                                value={newUser?.password}
                                                onChange={e => handleUserChange("password", e.target.value)}
                                                placeholder="Пароль"
                                            />
                                            <select
                                                value={newUser?.roleId}
                                                required
                                                onChange={e => {
                                                    handleUserChange("roleId", e.target.value)
                                                }}
                                            >
                                                <option value="">Выберите роль</option>
                                                {roles.map(r => (
                                                    <option key={r.id} value={r.id}>{r.name}</option>
                                                ))}
                                            </select>

                                            <button className="btn" type="submit">Зарегистрироваться</button>

                                            <p>
                                                Уже есть аккаунт? <a onClick={() => {
                                                resetMessage(setMessage, setMessageId);
                                                setShowRegistrationFrom(false);
                                                setShowLoginFrom(true)
                                            }}>Войти</a>
                                            </p>
                                        </form>
                                    </>}

                                {message && Math.floor(messageId / 10) === 0 &&(
                                    showMessage(message, messageId, setMessage, setMessageId)
                                )}

                            </>)
                        }
                    </div>
                </div>
            </div>*/}

        </div>





    )
}

export const getUserData = async () => {
    try {
        const response = await axios.get('http://localhost:8080/api/user/data');
        return response.data;
    }
    catch (e) {
        console.error("Пользователь не авторизован");
        return null;
    }
}


export default Auth;