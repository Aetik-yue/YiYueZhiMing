package com.example.yiyuezhiming.data

import androidx.compose.ui.graphics.Color
import com.example.yiyuezhiming.data.local.MemoryEntity
import com.example.yiyuezhiming.data.local.MemoEntity
import com.example.yiyuezhiming.data.local.AlbumPhotoEntity
import com.example.yiyuezhiming.data.local.ReminderEntity
import com.example.yiyuezhiming.model.AlbumPhoto
import com.example.yiyuezhiming.model.AnimalFace
import com.example.yiyuezhiming.model.Memory
import com.example.yiyuezhiming.model.Memo
import com.example.yiyuezhiming.model.Mood
import com.example.yiyuezhiming.model.Reminder

fun MemoryEntity.toModel(): Memory = Memory(
    id = id,
    date = date,
    mood = Mood(moodLabel, runCatching { AnimalFace.valueOf(moodFace) }.getOrDefault(AnimalFace.BunnyHappy), Color(imageColorArgb)),
    note = note,
    songTitle = songTitle,
    artistName = artistName,
    musicUri = musicUri,
    photoUri = photoUri,
    imageColorArgb = imageColorArgb,
    category = category,
    createdAt = createdAt
)

fun Memory.toEntity(): MemoryEntity = MemoryEntity(
    id = id,
    date = date,
    moodLabel = mood.label,
    moodFace = mood.face.name,
    note = note,
    songTitle = songTitle,
    artistName = artistName,
    musicUri = musicUri,
    photoUri = photoUri,
    imageColorArgb = imageColorArgb,
    category = category,
    createdAt = createdAt
)

fun MemoEntity.toModel(): Memo = Memo(
    id = id,
    title = title,
    content = content,
    category = category,
    isPinned = isPinned,
    isDone = isDone,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Memo.toEntity(): MemoEntity = MemoEntity(
    id = id,
    title = title,
    content = content,
    category = category,
    isPinned = isPinned,
    isDone = isDone,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun AlbumPhotoEntity.toModel(): AlbumPhoto = AlbumPhoto(
    id = id,
    category = category,
    uri = uri,
    memoryTag = memoryTag,
    takenDate = takenDate,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun AlbumPhoto.toEntity(): AlbumPhotoEntity = AlbumPhotoEntity(
    id = id,
    category = category,
    uri = uri,
    memoryTag = memoryTag,
    takenDate = takenDate,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ReminderEntity.toModel(): Reminder = Reminder(
    id = id,
    title = title,
    type = type,
    date = date,
    isEnabled = isEnabled,
    createdAt = createdAt
)

fun Reminder.toEntity(): ReminderEntity = ReminderEntity(
    id = id,
    title = title,
    type = type,
    date = date,
    isEnabled = isEnabled,
    createdAt = createdAt
)
