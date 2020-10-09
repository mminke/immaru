import {Collection} from '../repositories/CollectionRepository'

export default class AssetRepository {

    static headers: HeadersInit = {'Accept': 'application/json'}

    async assetsFor(collection: Collection) {
        let assets = fetch('/collections/' + collection.id + '/assets', {headers: AssetRepository.headers})
            .then(response => {
                if(!response.ok) {
                    console.error("Error retrieving assets")
                    return response.json()
                } else {
                    return response.json()
                }
            })
            .catch(error => {
                console.error('Error retrieving assets:', error)
            })

        return assets
    }

    async saveIn(collection: Collection, files: File[]) {
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

        let result = fetch('/collections/' + collection.id + '/assets', {
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

export type Asset = {
    id: string,
    collectionId: string
    originalFilename: string,
}
