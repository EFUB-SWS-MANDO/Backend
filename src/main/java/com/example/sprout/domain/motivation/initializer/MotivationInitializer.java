package com.example.sprout.domain.motivation.initializer;

import com.example.sprout.domain.motivation.entity.Motivation;
import com.example.sprout.domain.motivation.repository.MotivationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MotivationInitializer implements CommandLineRunner {

    private final MotivationRepository motivationRepository;

    List<String> motivationContents = List.of(
            "가장 큰 변화는 어느 특별한 하루가 아니라, 아무렇지 않은 오늘에서 시작된다.",
            "지금의 불안은 성장하고 있다는 증거다.",
            "늦게 도착해도 괜찮다. 중요한 건 멈추지 않는 것이다.",
            "기회는 준비된 순간이 아니라, 준비를 계속한 사람에게 찾아온다.",
            "결과는 통제할 수 없지만, 오늘의 노력은 통제할 수 있다.",
            "조금 느려도 괜찮다. 뒤로 가지만 않으면 된다.",
            "누군가는 오늘도 시작하고, 누군가는 오늘도 포기한다. 내일이 달라지는 사람은 시작한 사람이다.",
            "남들이 쉬는 날 쌓은 하루가, 나중엔 큰 차이가 된다.",
            "결과는 내일 오지만, 선택은 오늘 한다.",
            "오늘의 노력은 절대 사라지지 않는다.",
            "멈추는 순간, 가능성도 멈춘다.",
            "오늘도 해냈다면, 내일은 더 강해진다.",
            "두려움보다 한 걸음이 빠르다",
            "포기는 편하지만, 후회는 오래간다.",
            "하루에 하나씩, 부딪히는 걸 두려워하지 말자. 오늘의 노력이 내일의 합격을 만든다."
            );

    List<Motivation> motivations = new ArrayList<>();

    @Override
    public void run(String... args) throws Exception {
        if (motivationRepository.count() > 0) {
            return;
        }

        for (int i = 0; i < motivationContents.size(); i++) {
            motivations.add(
                    Motivation.builder()
                            .content(motivationContents.get(i))
                            .displayOrder(i + 1)
                            .build()
            );
        }

        motivationRepository.saveAll(motivations);
    }

}
