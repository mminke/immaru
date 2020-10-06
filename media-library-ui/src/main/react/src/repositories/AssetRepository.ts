export default class AssetRepository {

    static headers: HeadersInit = {'Accept': 'application/json'}

    async assets() {
        let assets = fetch('/assets', {headers: AssetRepository.headers})
            .then(response => response.json())

        return assets
    }

    async save(files: File[]) {
        const formData = new FormData()

        files.forEach( (file: File) => {
            console.log('File:', file)
            formData.append(
                "files",
                file,
                file.name
            )
        })

        console.log('formData: ', formData)

        let result = fetch('/assets', {
                method: 'POST',
                headers: AssetRepository.headers,
                body: formData
            })
            .then(response => response.json())
            .catch(error => {
                console.error('Error uploading files:', error)
            })

        console.log('Successfully uploaded files: ', result)
    }
}
