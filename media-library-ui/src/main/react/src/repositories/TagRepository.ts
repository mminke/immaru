export default class TagRepository {

    static headers: HeadersInit = {'Accept': 'application/json', 'Content-Type':'application/json'}

    async tags(collectionId: string) {
        let tags = fetch('/collections/' + collectionId + '/tags', {headers: TagRepository.headers})
            .then(response => {
                if(!response.ok) {
                    return []
                } else {
                    return response.json()
                }
            })

        return tags
    }

    async tagById(collectionId: string, tagId: string) {
        let tag = fetch('/collections/' + collectionId + '/tags/' + tagId, {headers: TagRepository.headers})
            .then(response => {
                if(!response.ok) {
                    return null
                } else {
                    return response.json()
                }
            })

        return tag
    }

    async create(collectionId: string, tag: {"name": String}) {
        let result = fetch('/collections/' + collectionId + '/tags/' , {
                        method: 'POST',
                        headers: TagRepository.headers,
                        body: JSON.stringify(tag)
                    })
                    .then(response => response.json())
                    .catch(error => {
                        console.error('Error creating new tag:', error)
                    })
    }
}

export type Tag = {
    id: string,
    name: string,
}
