const apiUrl = '/api/info';
const dataContainer = document.getElementById('data-container');

fetch(apiUrl)
    .then(response => response.json())
    .then(data => {
        const projectNameElement = document.createElement('p');
        projectNameElement.textContent = `Project name: ${data.project.name}`;
        dataContainer.appendChild(projectNameElement);

        const versionElement = document.createElement('p');
        versionElement.textContent = `Project version: ${data.project.version}`;
        dataContainer.appendChild(versionElement);

        const buildTimeElement = document.createElement('p');
        buildTimeElement.textContent = `Build time: ${data.build.time}`;
        dataContainer.appendChild(buildTimeElement);

        const gitCommitHashElement = document.createElement('p');
        gitCommitHashElement.textContent = `Git commit hash: ${data.git.commit.shortHash}`;
        dataContainer.appendChild(gitCommitHashElement);
    })
    .catch(error => {
        console.error('Error fetching data:', error);
        dataContainer.textContent = 'Error loading data.';
    });