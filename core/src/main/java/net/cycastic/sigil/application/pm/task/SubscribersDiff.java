package net.cycastic.sigil.application.pm.task;

import an.awesome.pipelinr.repack.com.google.common.base.Equivalence;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.model.pm.Task;
import net.cycastic.sigil.domain.model.pm.TaskSubscriber;
import net.cycastic.sigil.domain.model.tenant.User;
import net.cycastic.sigil.domain.repository.pm.TaskSubscriberRepository;

import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class SubscribersDiff {
    private static final Equivalence<User> USER_EQUIVALENCE = new Equivalence<>() {
        @Override
        protected boolean doEquivalent(User a, User b) {
            return Objects.equals(a, b) || Objects.equals(a.getNormalizedEmail(), b.getNormalizedEmail());
        }

        @Override
        protected int doHash(User item) {
            return item.getId();
        }
    };

    private final HashSet<Equivalence.Wrapper<User>> subscribed = new HashSet<>();
    private final TaskSubscriberRepository taskSubscriberRepository;
    private final Task task;

    public SubscribersDiff(TaskSubscriberRepository taskSubscriberRepository, Task task, User user){
        this(taskSubscriberRepository, task);
        subscribe(user);
    }

    public void subscribe(User user){
        var wrapped = USER_EQUIVALENCE.wrap(user);
        subscribed.add(wrapped);
    }

    private TaskSubscriber toSubscriber(Equivalence.Wrapper<User> wrapped){
        return TaskSubscriber.builder()
                .task(task)
                .subscriber(wrapped.get())
                .build();
    }

    public void apply(){
        if (subscribed.isEmpty()){
            return;
        }
        var existingSubscribers = taskSubscriberRepository.getFilteredSubscribedUsers(subscribed.stream().map(Equivalence.Wrapper::get).toList(), task).stream()
                .map(USER_EQUIVALENCE::wrap)
                .collect(Collectors.toSet());
        var newSubscription = subscribed.stream()
                .filter(w -> !existingSubscribers.contains(w))
                .map(this::toSubscriber)
                .toList();
        if (newSubscription.isEmpty()){
            return;
        }
        taskSubscriberRepository.saveAll(newSubscription);
        subscribed.clear();
    }
}
