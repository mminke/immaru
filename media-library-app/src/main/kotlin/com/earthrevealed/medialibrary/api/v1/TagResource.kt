package com.earthrevealed.medialibrary.api.v1

import com.earthrevealed.medialibrary.domain.TagId
import com.earthrevealed.medialibrary.domain.tag
import com.earthrevealed.medialibrary.persistence.TagRepository
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@ApiResponse
@RestController
@RequestMapping("/collections")
//TODO: Associate tags with a specific collection
class TagResource(
        val tagRepository: TagRepository
) {

    @GetMapping("/tags", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun tags() =
            tagRepository.all()

    @GetMapping("/tags/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun tag(@PathVariable("id") id: UUID) =
        tagRepository.get(TagId(id))

    @PostMapping("/tags", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createTag( @RequestBody newTag: NewTag) {
        val tag = tag {
            name = newTag.name
        }
        tagRepository.save(tag)
    }
}

data class NewTag(var name: String) {
    constructor(): this("unknown")
}