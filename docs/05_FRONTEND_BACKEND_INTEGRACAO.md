# 🌐 FRONTEND ↔ BACKEND - GUIA DE INTEGRAÇÃO

## 🎯 VISÃO GERAL DA INTEGRAÇÃO

### Arquitetura Típica
```
Frontend (React/Vue/Angular)     Backend (Spring Boot)
Port: 3000                       Port: 8080
├── Login/Register               ├── JWT Authentication
├── Dashboard                    ├── CRUD APIs
├── Forms                        ├── Validation
└── HTTP Requests                └── Database
```

### Tecnologias Comuns
- **Frontend**: React, Vue.js, Angular, Vanilla JS
- **Backend**: Spring Boot (nossa API)
- **Comunicação**: HTTP/REST + JSON
- **Autenticação**: JWT tokens

---

## 🔧 CONFIGURAÇÃO INICIAL

### 1. CORS no Backend (já configurado)
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000")  // Frontend URL
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*");
    }
}
```

### 2. URLs Base
```javascript
// Frontend
const API_BASE_URL = 'http://localhost:8080/api';

// Endpoints
const ENDPOINTS = {
    auth: {
        login: `${API_BASE_URL}/auth/login`,
        register: `${API_BASE_URL}/auth/register`,
        me: `${API_BASE_URL}/auth/me`
    },
    autores: `${API_BASE_URL}/autores`,
    livros: `${API_BASE_URL}/livros`
};
```

---

## 🔐 AUTENTICAÇÃO JWT

### 1. Service de Autenticação (JavaScript)
```javascript
class AuthService {
    // Login
    async login(username, password) {
        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, password })
            });

            if (!response.ok) {
                throw new Error('Login falhou');
            }

            const data = await response.json();
            
            // Armazenar token
            localStorage.setItem('token', data.token);
            localStorage.setItem('username', data.username);
            
            return data;
        } catch (error) {
            console.error('Erro no login:', error);
            throw error;
        }
    }

    // Registro
    async register(username, password) {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            throw new Error('Registro falhou');
        }

        const data = await response.json();
        
        // Armazenar token automaticamente
        localStorage.setItem('token', data.token);
        localStorage.setItem('username', data.username);
        
        return data;
    }

    // Logout
    logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        window.location.href = '/login';
    }

    // Verificar se está logado
    isAuthenticated() {
        return localStorage.getItem('token') !== null;
    }

    // Pegar token
    getToken() {
        return localStorage.getItem('token');
    }

    // Pegar usuário atual
    getCurrentUser() {
        return localStorage.getItem('username');
    }
}

const authService = new AuthService();
```

### 2. HTTP Client com JWT
```javascript
class ApiClient {
    constructor() {
        this.baseURL = 'http://localhost:8080/api';
    }

    // Método genérico para requisições
    async request(endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`;
        
        // Headers padrão
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        // Adicionar JWT se estiver logado
        const token = authService.getToken();
        if (token) {
            headers.Authorization = `Bearer ${token}`;
        }

        try {
            const response = await fetch(url, {
                ...options,
                headers
            });

            // Tratar erro 401 (token expirado)
            if (response.status === 401) {
                authService.logout();
                return;
            }

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.message || 'Erro na requisição');
            }

            // Retornar JSON se houver conteúdo
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return await response.json();
            }

            return response;
        } catch (error) {
            console.error('Erro na API:', error);
            throw error;
        }
    }

    // Métodos HTTP
    get(endpoint) {
        return this.request(endpoint);
    }

    post(endpoint, data) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }

    put(endpoint, data) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    }

    delete(endpoint) {
        return this.request(endpoint, {
            method: 'DELETE'
        });
    }
}

const apiClient = new ApiClient();
```

---

## 📚 CRUD OPERATIONS

### 1. Service de Autores
```javascript
class AutorService {
    // Listar todos
    async getAll() {
        return await apiClient.get('/autores');
    }

    // Buscar por ID
    async getById(id) {
        return await apiClient.get(`/autores/${id}`);
    }

    // Criar novo
    async create(autor) {
        return await apiClient.post('/autores', autor);
    }

    // Atualizar
    async update(id, autor) {
        return await apiClient.put(`/autores/${id}`, autor);
    }

    // Deletar
    async delete(id) {
        return await apiClient.delete(`/autores/${id}`);
    }
}

const autorService = new AutorService();
```

### 2. Service de Livros
```javascript
class LivroService {
    async getAll() {
        return await apiClient.get('/livros');
    }

    async getById(id) {
        return await apiClient.get(`/livros/${id}`);
    }

    async create(livro) {
        return await apiClient.post('/livros', livro);
    }

    async update(id, livro) {
        return await apiClient.put(`/livros/${id}`, livro);
    }

    async delete(id) {
        return await apiClient.delete(`/livros/${id}`);
    }
}

const livroService = new LivroService();
```

---

## 🎨 EXEMPLOS DE COMPONENTES

### 1. Formulário de Login (Vanilla JS)
```html
<!DOCTYPE html>
<html>
<head>
    <title>Login</title>
</head>
<body>
    <form id="loginForm">
        <div>
            <label>Usuário:</label>
            <input type="text" id="username" required>
        </div>
        <div>
            <label>Senha:</label>
            <input type="password" id="password" required>
        </div>
        <button type="submit">Entrar</button>
    </form>

    <div id="error" style="color: red; display: none;"></div>

    <script>
        document.getElementById('loginForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;
            const errorDiv = document.getElementById('error');

            try {
                await authService.login(username, password);
                window.location.href = '/dashboard.html';
            } catch (error) {
                errorDiv.textContent = 'Usuário ou senha incorretos';
                errorDiv.style.display = 'block';
            }
        });
    </script>
</body>
</html>
```

### 2. Lista de Autores (Vanilla JS)
```html
<!DOCTYPE html>
<html>
<head>
    <title>Autores</title>
</head>
<body>
    <h1>Autores</h1>
    
    <button onclick="showCreateForm()">Novo Autor</button>
    
    <div id="autorList"></div>
    
    <!-- Modal para criar/editar -->
    <div id="modal" style="display: none;">
        <form id="autorForm">
            <input type="hidden" id="autorId">
            <div>
                <label>Nome:</label>
                <input type="text" id="nome" required>
            </div>
            <div>
                <label>Email:</label>
                <input type="email" id="email" required>
            </div>
            <div>
                <label>CEP:</label>
                <input type="text" id="cep" pattern="\d{5}-\d{3}" required>
            </div>
            <div>
                <label>Telefone:</label>
                <input type="text" id="telefone" pattern="\(\d{2}\) \d{5}-\d{4}" required>
            </div>
            <button type="submit">Salvar</button>
            <button type="button" onclick="hideModal()">Cancelar</button>
        </form>
    </div>

    <script>
        // Verificar autenticação
        if (!authService.isAuthenticated()) {
            window.location.href = '/login.html';
        }

        // Carregar autores
        async function loadAutores() {
            try {
                const autores = await autorService.getAll();
                const listDiv = document.getElementById('autorList');
                
                listDiv.innerHTML = autores.map(autor => `
                    <div>
                        <h3>${autor.nome}</h3>
                        <p>Email: ${autor.email}</p>
                        <p>CEP: ${autor.cep}</p>
                        <p>Telefone: ${autor.telefone}</p>
                        <button onclick="editAutor(${autor.idAutor})">Editar</button>
                        <button onclick="deleteAutor(${autor.idAutor})">Deletar</button>
                    </div>
                `).join('');
            } catch (error) {
                alert('Erro ao carregar autores: ' + error.message);
            }
        }

        // Criar/Editar autor
        document.getElementById('autorForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const autorData = {
                nome: document.getElementById('nome').value,
                email: document.getElementById('email').value,
                cep: document.getElementById('cep').value,
                telefone: document.getElementById('telefone').value
            };

            try {
                const autorId = document.getElementById('autorId').value;
                
                if (autorId) {
                    await autorService.update(autorId, autorData);
                } else {
                    await autorService.create(autorData);
                }
                
                hideModal();
                loadAutores();
            } catch (error) {
                alert('Erro ao salvar autor: ' + error.message);
            }
        });

        // Funções auxiliares
        function showCreateForm() {
            document.getElementById('autorForm').reset();
            document.getElementById('autorId').value = '';
            document.getElementById('modal').style.display = 'block';
        }

        async function editAutor(id) {
            try {
                const autor = await autorService.getById(id);
                
                document.getElementById('autorId').value = autor.idAutor;
                document.getElementById('nome').value = autor.nome;
                document.getElementById('email').value = autor.email;
                document.getElementById('cep').value = autor.cep;
                document.getElementById('telefone').value = autor.telefone;
                
                document.getElementById('modal').style.display = 'block';
            } catch (error) {
                alert('Erro ao carregar autor: ' + error.message);
            }
        }

        async function deleteAutor(id) {
            if (confirm('Tem certeza que deseja deletar este autor?')) {
                try {
                    await autorService.delete(id);
                    loadAutores();
                } catch (error) {
                    alert('Erro ao deletar autor: ' + error.message);
                }
            }
        }

        function hideModal() {
            document.getElementById('modal').style.display = 'none';
        }

        // Carregar dados iniciais
        loadAutores();
    </script>
</body>
</html>
```

---

## ⚛️ EXEMPLO REACT

### 1. Hook de Autenticação
```jsx
import { useState, useEffect, createContext, useContext } from 'react';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const token = localStorage.getItem('token');
        const username = localStorage.getItem('username');
        
        if (token && username) {
            setUser({ username, token });
        }
        
        setLoading(false);
    }, []);

    const login = async (username, password) => {
        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            if (!response.ok) throw new Error('Login falhou');

            const data = await response.json();
            
            localStorage.setItem('token', data.token);
            localStorage.setItem('username', data.username);
            
            setUser({ username: data.username, token: data.token });
            
            return data;
        } catch (error) {
            throw error;
        }
    };

    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, logout, loading }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth deve ser usado dentro de AuthProvider');
    }
    return context;
};
```

### 2. Componente de Lista de Autores
```jsx
import React, { useState, useEffect } from 'react';
import { useAuth } from './AuthContext';

const AutorList = () => {
    const [autores, setAutores] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const { user } = useAuth();

    useEffect(() => {
        loadAutores();
    }, []);

    const loadAutores = async () => {
        try {
            setLoading(true);
            const response = await fetch('/api/autores', {
                headers: {
                    'Authorization': `Bearer ${user.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) throw new Error('Erro ao carregar autores');

            const data = await response.json();
            setAutores(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const deleteAutor = async (id) => {
        if (!window.confirm('Tem certeza?')) return;

        try {
            const response = await fetch(`/api/autores/${id}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${user.token}`
                }
            });

            if (!response.ok) throw new Error('Erro ao deletar autor');

            loadAutores(); // Recarregar lista
        } catch (err) {
            alert('Erro ao deletar: ' + err.message);
        }
    };

    if (loading) return <div>Carregando...</div>;
    if (error) return <div>Erro: {error}</div>;

    return (
        <div>
            <h1>Autores</h1>
            
            {autores.map(autor => (
                <div key={autor.idAutor} style={{ border: '1px solid #ccc', margin: '10px', padding: '10px' }}>
                    <h3>{autor.nome}</h3>
                    <p>Email: {autor.email}</p>
                    <p>CEP: {autor.cep}</p>
                    <p>Telefone: {autor.telefone}</p>
                    
                    <button onClick={() => deleteAutor(autor.idAutor)}>
                        Deletar
                    </button>
                </div>
            ))}
        </div>
    );
};

export default AutorList;
```

---

## 🚨 TRATAMENTO DE ERROS

### 1. Interceptor Global de Erros
```javascript
class ErrorHandler {
    static handle(error, context = '') {
        console.error(`Erro em ${context}:`, error);

        // Erro de rede
        if (!navigator.onLine) {
            this.showError('Sem conexão com a internet');
            return;
        }

        // Erro 401 - Token expirado
        if (error.message.includes('401')) {
            this.showError('Sessão expirada. Faça login novamente.');
            authService.logout();
            return;
        }

        // Erro 403 - Sem permissão
        if (error.message.includes('403')) {
            this.showError('Você não tem permissão para esta ação');
            return;
        }

        // Erro 404 - Não encontrado
        if (error.message.includes('404')) {
            this.showError('Recurso não encontrado');
            return;
        }

        // Erro 409 - Conflito (email/ISBN duplicado)
        if (error.message.includes('409')) {
            this.showError('Dados duplicados. Verifique email ou ISBN.');
            return;
        }

        // Erro genérico
        this.showError(error.message || 'Erro inesperado');
    }

    static showError(message) {
        // Implementar notificação (toast, modal, etc.)
        alert(message); // Simples para hackathon
    }
}
```

### 2. Loading States
```javascript
class LoadingManager {
    static show(element) {
        element.disabled = true;
        element.textContent = 'Carregando...';
    }

    static hide(element, originalText) {
        element.disabled = false;
        element.textContent = originalText;
    }
}

// Uso
const button = document.getElementById('saveButton');
LoadingManager.show(button);

try {
    await autorService.create(data);
} finally {
    LoadingManager.hide(button, 'Salvar');
}
```

---

## 💡 DICAS PRO HACKATHON

### 1. Estrutura de Projeto Frontend
```
frontend/
├── index.html          # Página inicial
├── login.html          # Login
├── dashboard.html      # Dashboard principal
├── autores.html        # CRUD autores
├── livros.html         # CRUD livros
├── js/
│   ├── auth.js         # Serviço de autenticação
│   ├── api.js          # Cliente HTTP
│   ├── autores.js      # Serviço de autores
│   └── livros.js       # Serviço de livros
└── css/
    └── style.css       # Estilos
```

### 2. Validação Frontend
```javascript
function validateAutor(autor) {
    const errors = [];

    if (!autor.nome || autor.nome.length < 2) {
        errors.push('Nome deve ter pelo menos 2 caracteres');
    }

    if (!autor.email || !autor.email.includes('@')) {
        errors.push('Email inválido');
    }

    if (!autor.cep || !/^\d{5}-\d{3}$/.test(autor.cep)) {
        errors.push('CEP deve estar no formato XXXXX-XXX');
    }

    if (!autor.telefone || !/^\(\d{2}\) \d{5}-\d{4}$/.test(autor.telefone)) {
        errors.push('Telefone deve estar no formato (XX) XXXXX-XXXX');
    }

    return errors;
}
```

### 3. Máscaras de Input
```javascript
// Máscara para CEP
document.getElementById('cep').addEventListener('input', (e) => {
    let value = e.target.value.replace(/\D/g, '');
    if (value.length > 5) {
        value = value.substring(0, 5) + '-' + value.substring(5, 8);
    }
    e.target.value = value;
});

// Máscara para telefone
document.getElementById('telefone').addEventListener('input', (e) => {
    let value = e.target.value.replace(/\D/g, '');
    if (value.length > 0) {
        value = value.replace(/^(\d{2})(\d{5})(\d{4}).*/, '($1) $2-$3');
    }
    e.target.value = value;
});
```

---

## 🎯 CHECKLIST INTEGRAÇÃO

### Backend
- [ ] CORS configurado para localhost:3000
- [ ] Endpoints retornam JSON correto
- [ ] JWT funciona corretamente
- [ ] Validações retornam erros claros
- [ ] Swagger documentado

### Frontend
- [ ] Autenticação implementada
- [ ] Token armazenado no localStorage
- [ ] Authorization header enviado
- [ ] Erros 401 tratados (logout automático)
- [ ] Loading states implementados
- [ ] Validação de formulários
- [ ] Máscaras de input aplicadas
- [ ] Mensagens de erro amigáveis

### Testes de Integração
- [ ] Login/logout funciona
- [ ] CRUD completo de autores
- [ ] CRUD completo de livros
- [ ] Relacionamento autor-livro funciona
- [ ] Validações frontend/backend consistentes
- [ ] Tratamento de erros funciona