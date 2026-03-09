import {createContext, useState, useContext, useEffect} from 'react';
import {jwtDecode} from "jwt-decode";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {


    const [token, setToken] = useState(() =>
        localStorage.getItem("token"));

    const [user, setUser] = useState(null);

    useEffect(() => {

        if (!token) {
            return;
        }
        try {
            const decoded = jwtDecode(token);
            if (decoded.exp * 1000 > Date.now())
                setUser({login: decoded.sub, name: decoded.name});
            else {
                alert("Срок действия токена истек, авторизуйтесь заново");
                logout();
            }
        }
        catch (e) {
            console.error("Ошибка декодирования токена:", e);
            logout();
        }

    }, [token]);

    const login = (jwt) => {
        localStorage.setItem("token", jwt);
        setToken(jwt);
        const decoded = jwtDecode(jwt);
        setUser({login: decoded.sub, name: decoded.name});
    };

    const logout = () => {
        localStorage.removeItem("token");
        setToken(null);
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ token, login, logout, user }}>
            {children}
        </AuthContext.Provider>
    )

};

export const useAuth = () => useContext(AuthContext);
