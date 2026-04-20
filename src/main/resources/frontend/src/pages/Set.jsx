import React, {useEffect, useState} from 'react';
import api from "../api/axios";
import  '../styles/Set.css';
import { validateLinks } from "../util/validateLinks";

//TODO нельзя создать пустой шаблон
//TODO если нет сетов то выводится соответствующее сообщение

const MODAL_MODES = {
    CREATE: 'create',
    VIEW: 'view',
    UPDATE: 'update',
};

const FORM_FIELDS = [
    {
        type: 'text',
        name: 'name',
        placeholder: 'Название',
        required: true,
    },
    {
        type: 'textarea',
        name: 'links',
        placeholder: 'Список ссылок',
        required: true,
        rows: 10,
        cols: 50,
    },
];

export const getTemplates = async () => {
    const response = await api.get('/template/get');
    return response.data;
};

const Set = () => {

    const [isLoading, setIsLoading] = useState(false);
    const [templates, setTemplates] = useState([]);
    const [template, setTemplate] = useState();
    const [message, setMessage] = useState(null);
    const [modalMode, setModalMode] = useState(null);

    const isViewMode = modalMode === 'view';

    const modalConfig = {
        [MODAL_MODES.CREATE]: {
            title: 'Создание сета',
            submitText: 'Создать',
        },
        [MODAL_MODES.VIEW]: {
            title: 'Просмотр списка ссылок сета',
            submitText: null,
        },
        [MODAL_MODES.UPDATE]: {
            title: 'Редактирование сета',
            submitText: 'Сохранить',
        },
    };

    useEffect(() => {
        const loadTemplates = async () => {
            try {
                setIsLoading(true);
                const data = await getTemplates();
                setTemplates(data);
            } catch (e) {
                console.error(e);
                setMessage(e?.response?.data || e.message || "Ошибка при загрузке сетов");
            } finally {
                setIsLoading(false);
            }
        };

        loadTemplates();
    }, []);

    const closeModal = () => {
        setMessage(null);
        setModalMode(null);
        setTemplate(null);
    }

    const openCreateModal = () => {
        setMessage(null);
        setTemplate({
            id: null,
            name: '',
            links: '',
        });
        setModalMode(MODAL_MODES.CREATE);
    }

    const openViewModal = (template) => {
        setMessage(null);
        setTemplate({
            ...template,
            links: template.links.join('\n'),
        });
        setModalMode(MODAL_MODES.VIEW)
    }

    const openEditModal = (template) => {
        setMessage(null);
        setTemplate({
            ...template,
            links: template.links.join('\n'),
        });
        setModalMode(MODAL_MODES.UPDATE)
    }

    const handleFieldChange = (e) => {
        const { name, value } = e.target;

        setTemplate(prev => ({
            ...prev,
            [name]: value,
        }));
    };

    const createTemplate = async () => {

        const validation = validateLinks(template.links);

        if (!validation.valid) {
            setMessage(validation.error);
            return;
        }

        if (validation.errors.length > 0) {
            setMessage(validation.errors.join('\n'));
            return;
        }

        const payload = {
            ...template,
            links: validation.links
        };

        try {
            const response = await api.post(
                '/template/create', payload );
            setTemplates(prev => [...prev, response.data]);
            closeModal();
            //TODO правильное склонение ссылок
            setMessage(`Сет ${template.name} с ${validation.links.length} ссылок создан`);
        }
        catch (e) {
            console.error('Ошибка при создании сета', e);
        }
    }

    const updateTemplate = async () => {

        const validation = validateLinks(template.links);

        if (!validation.valid) {
            setMessage(validation.error);
            return;
        }

        if (validation.errors.length > 0)
        {
            setMessage(validation.errors.join('\n'));
            return;
        }

        template.links = validation.links;

        console.log(template);

        try {
            //TODO сделать чтобы setTemplates брал из response
            //TODO добавить проверку формата ссылок (вынести в отдельную функцию из parse)
            const response = await api.post(
                '/template/update', template);
            setTemplates(prev => prev.map(tmp => tmp.id === template.id ? template : tmp))
            closeModal();
            setMessage(`Сет ${template.name} успешно отредактирован`);
        }
        catch (e) {
            console.error('Ошибка при редактировании сета', e);
        }
    }

    const handleModalSubmit = async (e) => {
        e.preventDefault();

        if (modalMode === MODAL_MODES.CREATE) {
            await createTemplate()
        }

        if (modalMode === MODAL_MODES.UPDATE) {
            await updateTemplate()
        }

    }

     const deleteTemplate = async (id) => {

        try {
            console.log("Удаляю сет", id);
            await api.delete(`/template/delete/${id}`,
                {data: id},
            );
            setTemplates(prev => prev.filter(t => t.id !== id));
            setMessage("Сет успешно удалён");
        }
        catch (e) {
            console.error(e);
            setMessage(e?.response?.data || e.message || "Ошибка при удалении сета");
        }
    }

    return (
        <>
            {modalMode === null && message && <>{message}</>}

            {modalMode !== null && (
                <div className="modal-overlay" onClick={() => closeModal()}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <form onSubmit={(e) => handleModalSubmit(e)}>
                            <h2>{modalConfig[modalMode].title}</h2>

                            {message && (
                                <div style={{ whiteSpace: 'pre-line' }}>
                                    {message}
                                </div>
                            )}

                            {FORM_FIELDS.map((field) => {
                                const value = template[field.name] ?? '';
                                const props = {
                                    key: field.name,
                                    name: field.name,
                                    placeholder: field.placeholder,
                                    className: 'input',
                                    value,
                                    required: !isViewMode && field.required,
                                    readOnly: isViewMode,
                                    onChange: isViewMode ? undefined : handleFieldChange,
                                };

                                if (field.type === 'textarea') {
                                    return (
                                        <textarea
                                            {...props}
                                            rows={field.rows}
                                            cols={field.cols}
                                        />
                                    );
                                }

                                return (
                                    <input
                                        {...props}
                                        type={field.type}
                                    />
                                );
                            })}


                            <div className="form-actions">
                                <button type="button" onClick={closeModal}>
                                    Закрыть
                                </button>
                                {!isViewMode && (
                                    <button type="submit">
                                        {modalConfig[modalMode].submitText}
                                    </button>
                                )}
                            </div>
                        </form>
                    </div>
                </div>
            )}

            <div className="sets-heading">
                <h2 className="sets-heading__title">Управление сетами</h2>

                    <i className="bi bi-plus-circle-fill"
                       title="Создать новый сет"
                       onClick={openCreateModal}
                    ></i>
            </div>

            {isLoading && <>Загрузка</>}

            {!isLoading  && templates.length === 0 ? <>У вас пока нет сетов</> :
                <table className="table">
                    <tbody>
                    {templates.map((t, i) => (t &&
                    <tr key={t.id}>
                        <td>{i + 1}</td>
                        <td>{t.name}</td>
                        <td>
                            <i className="bi bi-info-circle" title="Информация о сете" onClick={() => {
                                openViewModal(t)
                            }}/>
                        </td>
                        <td>
                        <i className="bi bi-pencil-fill" title="Редактировать" onClick={() => {
                                openEditModal(t)
                            }}/>

                        </td>
                        <td>
                            <i className="bi bi-trash3-fill" title="Удалить" onClick={() => deleteTemplate(t.id)}/>
                        </td>
                    </tr>


                    ))}
                    </tbody>

                </table>
            }
        </>
    )

}

export default Set;
