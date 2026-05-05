const apiUrl = '/api/build-info';

const dataContainer = document.getElementById('data-container');
const appTitle = document.getElementById('app-title');
const rootNameValue = document.getElementById('root-name-value');
const projectNameValue = document.getElementById('project-name-value');
const projectVersionValue = document.getElementById('project-version-value');
const infoRowTemplate = document.getElementById('build-info-row-template');
const infoRowMonoTemplate = document.getElementById('build-info-row-mono-template');
const infoErrorTemplate = document.getElementById('build-info-error-template');
const serviceLinks = document.querySelectorAll('[data-service-url]');

serviceLinks.forEach(link => {
    link.href = link.dataset.serviceUrl;
    link.target = '_blank';
    link.rel = 'noopener noreferrer';
});

function safeValue(value, fallback = 'Unavailable') {
    return value ?? fallback;
}

function createInfoRow(label, value, useMonospace = false) {
    const template = useMonospace ? infoRowMonoTemplate : infoRowTemplate;
    const row = template.content.firstElementChild.cloneNode(true);
    const labelElement = row.querySelector('.info-label');
    const valueElement = row.querySelector('.info-value');

    labelElement.textContent = label;
    valueElement.textContent = safeValue(value);

    return row;
}

function renderBuildInfo(data) {
    const project = data?.project ?? {};
    const gitCommit = data?.git?.commit ?? {};
    const rootName = safeValue(project.root?.name, 'Immaru');
    const fullCommitHash = safeValue(gitCommit.hash);

    appTitle.textContent = rootName;
    document.title = `${rootName} - info`;

    rootNameValue.textContent = rootName;
    projectNameValue.textContent = safeValue(project.name);
    projectVersionValue.textContent = safeValue(project.version);

    dataContainer.replaceChildren(
        createInfoRow('Build time', data?.build?.time),
        createInfoRow('Git commit', fullCommitHash, true)
    );
}

function renderErrorState() {
    appTitle.textContent = 'Immaru';
    document.title = 'Immaru - info';

    rootNameValue.textContent = 'Unavailable';
    projectNameValue.textContent = 'Unavailable';
    projectVersionValue.textContent = 'Unavailable';

    const errorMessage = infoErrorTemplate.content.firstElementChild.cloneNode(true);
    errorMessage.textContent = 'Build information is currently unavailable.';

    dataContainer.replaceChildren(errorMessage);
}

fetch(apiUrl)
    .then(response => {
        if (!response.ok) {
            throw new Error(`Unexpected response status: ${response.status}`);
        }

        return response.json();
    })
    .then(renderBuildInfo)
    .catch(error => {
        console.error('Error fetching data:', error);
        renderErrorState();
    });
