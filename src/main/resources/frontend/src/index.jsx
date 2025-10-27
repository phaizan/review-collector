import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import Auth from "./Pages/Auth";
import Main from "./Pages/Main";
import {AuthProvider, useAuth} from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";

const Index = () => {

    const { user, removeUser } = useAuth();

    return (
            <div className="body">
                {user && (
                    <div className="hello">
                        <span>Добро пожаловать, {user.name}</span>
                        <button onClick={removeUser}>Выйти</button>
                    </div>
                )}
                    <Routes>
                        <Route path="/" element={<Auth/>}/>
                        <Route
                            path="/main"
                            element={
                                <ProtectedRoute>
                                    <Main/>
                                </ProtectedRoute>
                        }/>
                    </Routes>
            </div>
    );
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
    <React.StrictMode>
        <BrowserRouter>
            <AuthProvider>
                <Index />
            </AuthProvider>
        </BrowserRouter>
    </React.StrictMode>
);
