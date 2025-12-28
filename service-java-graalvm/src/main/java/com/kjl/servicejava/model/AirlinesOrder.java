package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class AirlinesOrder extends BaseOrder {
    @JsonProperty("Items")
    private List<AirlinesItem> items;
}
