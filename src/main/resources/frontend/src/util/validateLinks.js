const SUPPORTED_HOSTS = [
    'otzovik.com',
    'irecommend.ru',
    'www.otzyvru.com',
    'flamp.ru',
    '2gis.ru',
    'yandex.ru',
    'yandex.com',
    'ozon.ru'
];

const isSupportedHost = (hostname) => {
    return SUPPORTED_HOSTS.some(host =>
        hostname === host || hostname.endsWith(`.${host}`)
    );
};

export const validateLinks = (input) => {
    const links = input
        .split(/[\s,;]+/)
        .map(l => l.trim())
        .filter(Boolean);

    if (links.length > 50) {
        return { valid: false, error: `Можно указать не более 50 ссылок, указано ${links.length}` };
    }

    const uniqueLinks = [...new Set(links)];

    const errors = [];

    uniqueLinks.forEach((link, index) => {

        let url;

        try {
            url = new URL(link);
        } catch {
            errors.push(`Ссылка №${index + 1} имеет некорректный формат: ${link}`)
            return;
        }

        if (!['http:', 'https:'].includes(url.protocol)) {
            errors.push(`Ссылка №${index + 1} имеет недопустимый протокол: ${link}`);
        }

        if (!isSupportedHost(url.hostname.toLowerCase())) {
            errors.push(`Ссылка №${index + 1} ведёт на неподдерживаемый источник: ${link}`);
        }

    })
    return {
        valid: true,
        errors,
        links: uniqueLinks
    };
};