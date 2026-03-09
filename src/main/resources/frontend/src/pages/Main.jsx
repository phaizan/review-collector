import React from 'react';
import { useAuth } from "../context/AuthProvider";
import { NavLink, Outlet } from "react-router-dom";
import  '../styles/NavBar.css'

const Main = () => {

    const { user, logout } = useAuth();

    return (
        <div className="body">
            <header className="navbar">

                <div className="logo">Revy</div>

                <nav>
                    <NavLink to="parser" style={({isActive}) => ({fontWeight: isActive ? 'bold' : 'normal'})}>
                        Парсер
                    </NavLink>

                    <NavLink to="history" style={({isActive}) => ({fontWeight: isActive ? 'bold' : 'normal'})}>
                        История
                    </NavLink>
                </nav>

                <div className="user-block">
                    <span>Добро пожаловать, {user?.name}</span>
                    <button onClick={logout}>Выйти</button>
                </div>

            </header>
            <main>
                <Outlet />
            </main>

        </div>
    )

}

export default Main;
