package com.example.rag.business.abstracts;

import java.util.List;

public interface ChunkingService {

    List<String> chunk(String text);

}
