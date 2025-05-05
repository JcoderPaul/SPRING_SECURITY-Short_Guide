package me.oldboy.likebase;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LikeBaseTest {

    @Autowired
    private LikeBase likeBase;

    @Test
    void shouldReturnListSizeLoadUserList() {
        /* При формировании тестового контекста метод *.loadUserList() уже вызывался, а значит это повторный вызов ... */
        likeBase.loadUserList();

        /*
        Мы знаем, что метод *.loadUserList() при вызове добавляет в коллекцию 2-а элемента, т.е. теперь
        у нас в коллекции 4-и таковых, первые два были помещены туда при формировании контекста, два
        других при ручном вызове, проделанном выше.
        */
        assertThat(likeBase.getUserList().size()).isEqualTo(4);
    }
}