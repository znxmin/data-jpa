package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.datajpa.entity.Item;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository;

    @Test
    void save() {
        // 식별자 생성 전략이 @GeneratedValue가 아닐 경우
        // save() 최초 호출 시점에도 식별자 값이 존재하기 때문에
        // merge() 가 호출된다.
        // merge는 우선 DB를 호출해서 값을 확인하고, DB에 값이 없으면 새로운 엔티티로 인식하기 때문에 비효율적이다.
        // 따라서 이런 경우에는 @CreatedDate와 같은 속성과
        // Persistable를 사용해서 새로운 엔티티 확인 여부를 직접 구현하는게 효과적이다.
        Item item = new Item("A");
        itemRepository.save(item);
    }

}