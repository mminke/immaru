import {Collection} from '../repositories/CollectionRepository'

export type Asset = {
    id: string,
    collectionId: string,
    originalFilename: string,
    tagIds: string[]
}

export default class AssetRepository {

    static headers: HeadersInit = {'Accept': 'application/json'}
    static headersForUpdate: HeadersInit = {'Accept': 'application/json', 'Content-Type':'application/json'}

    async assetsFor(collection: Collection) {
        let assets = fetch('/collections/' + collection.id + '/assets', {headers: AssetRepository.headers})
            .then(response => {
                if(!response.ok) {
                    console.error("Error retrieving assets", response)
                    return []
                } else {
                    return response.json()
                }
            })
            .catch(error => {
                console.error('Error retrieving assets:', error)
            })

        return assets
    }

    async assetWithId(collection: Collection, id: string) {
        let asset = fetch('/collections/' + collection.id + '/assets/' + id, {headers: AssetRepository.headers})
            .then(response => {
                if(!response.ok) {
                    console.error("Error retrieving assets", response)
                    return null
                } else {
                    return response.json()
                }
            })
            .catch(error => {
                console.error('Error retrieving assets:', error)
            })

        return asset
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

        const result = fetch('/collections/' + collection.id + '/assets', {
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

    async updateTagsFor(asset: Asset) {
        this.deduplicateTagIds(asset)

        const data = JSON.stringify(asset.tagIds)

        fetch('/collections/' + asset.collectionId + '/assets/' + asset.id + "/tags", {
            method: 'PUT',
            headers: AssetRepository.headersForUpdate,
            body: data
        })
        .then(response => response.json())
        .catch(error => {
            console.error('Error updating tags for asset.', error)
        })
    }

    deduplicateTagIds(asset: Asset) {
        asset.tagIds = Array.from(new Set(asset.tagIds))
    }
}

export const assetRepository = new AssetRepository()
