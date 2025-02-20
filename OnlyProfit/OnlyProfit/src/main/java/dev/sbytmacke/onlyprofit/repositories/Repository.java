package dev.sbytmacke.onlyprofit.repositories;

import dev.sbytmacke.onlyprofit.dto.UserDTO;

import java.util.List;

public interface Repository<T, ID> {
    T addItem(T item);

    List<UserDTO> getAll();
}

