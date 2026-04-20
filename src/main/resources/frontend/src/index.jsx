import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import Auth from "./pages/Auth";
import Main from "./pages/Main";
import { AuthProvider } from "./context/AuthProvider";
import ProtectedRoute from "./components/ProtectedRoute";
import History from "./pages/History";
import Parser from "./pages/Parser";
import Set from "./pages/Set";
import 'bootstrap-icons/font/bootstrap-icons.css';

const Index = () => {

    return (
        <div>
            <BrowserRouter>
                <Routes>
                    <Route path="/login" element={<Auth/>}/>
                    <Route element={<ProtectedRoute/>}>
                        <Route path="/main" element={<Main/>}>
                            <Route index element={<Parser/>}/>
                            <Route path="set" element={<Set/>}/>
                            <Route path="parser" element={<Parser/>}/>
                            <Route path="history" element={<History/>}/>
                        </Route>
                    </Route>
                    <Route path="*" element={<Auth />}/>
                </Routes>
            </BrowserRouter>
        </div>
    );
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
    <AuthProvider>
        <Index />
    </AuthProvider>
);
