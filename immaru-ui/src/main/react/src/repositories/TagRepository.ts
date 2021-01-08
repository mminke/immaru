export type Tag = {
    id: string,
    name: string
}

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
        const result = fetch('/collections/' + collectionId + '/tags/' , {
            method: 'POST',
            headers: TagRepository.headers,
            body: JSON.stringify(tag)
        })
        .then(response => {
            if(!response.ok) {
                return new Promise( (resolve) => { resolve([] as Tag[]) })
            } else {
                const promise = response.json()
                    .then( (json) => {
                        return Promise.all(
                            json.locations.map( (location: string) => {
                                return fetch(location, {
                                    method: 'GET',
                                    headers: TagRepository.headers,
                                }).then(response => {
                                    if(!response.ok) {
                                        return null
                                    } else {
                                        return response.json()
                                    }
                                })
                            })
                        )
                    })

                return promise
            }
        })
        .catch(error => {
            console.error('Error creating new tag:', error)
            return new Promise( (resolve) => { resolve([] as Tag[]) })
        })

        return result
    }
}

export const tagRepository = new TagRepository()
