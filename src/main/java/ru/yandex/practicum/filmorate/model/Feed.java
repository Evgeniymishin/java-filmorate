package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

@Data
@NoArgsConstructor
public class Feed {
    private Integer eventId;
    private Integer userId;
    private Integer entityId;
    private Operation operation;
    private EventType eventType;
    private Long timestamp;
}
