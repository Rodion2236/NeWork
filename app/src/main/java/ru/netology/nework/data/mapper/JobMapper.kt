package ru.netology.nework.data.mapper

import ru.netology.nework.data.remote.dto.JobDto
import ru.netology.nework.domain.model.Job

fun Job(dto: JobDto): Job = Job(
    id = dto.id.toString(),
    name = dto.name,
    position = dto.position,
    start = dto.start,
    finish = dto.finish,
    link = dto.link
)

fun JobDto(job: Job): JobDto = JobDto(
    id = job.id.toInt(),
    name = job.name,
    position = job.position,
    start = job.start,
    finish = job.finish,
    link = job.link
)