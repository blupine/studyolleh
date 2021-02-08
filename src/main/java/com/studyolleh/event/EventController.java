package com.studyolleh.event;

import com.studyolleh.account.CurrentAccount;
import com.studyolleh.domain.Account;
import com.studyolleh.domain.Event;
import com.studyolleh.domain.Study;
import com.studyolleh.event.form.EventForm;
import com.studyolleh.event.validator.EventValidator;
import com.studyolleh.study.StudyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/study/{path}")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final EventValidator eventValidator;
    private final StudyService studyService;
    private final ModelMapper modelMapper;

    @InitBinder("eventForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(eventValidator);
    }

    @GetMapping("/new-event")
    public String newEventForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(new EventForm());
        return "event/form";
    }

    @PostMapping("/new-event")
    public String newEvent(@CurrentAccount Account account, @PathVariable String path,
                           @Valid EventForm eventForm, Errors errors, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            return "event/form";
        }

        Event event = eventService.createEvent(modelMapper.map(eventForm, Event.class), study, account);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{id}")
    public String getEvent(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long id,
                           Model model) {
        model.addAttribute(account);
        model.addAttribute(studyService.findStudyWithManagersByPath(path));
        model.addAttribute(eventService.findEventById(id));
        return "/event/view";
    }

    @GetMapping("/events")
    public String getEvents(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(study);
        List<Event> eventList = eventService.findByStudyOrderByStartDateTime(study);

        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();

        eventList.forEach(e -> {
            if (e.getEndDateTime().isBefore(LocalDateTime.now())) {
                oldEvents.add(e);
            } else {
                newEvents.add(e);
            }
        });

        model.addAttribute("oldEvents", oldEvents);
        model.addAttribute("newEvents", newEvents);
        return "study/events";
    }

    @GetMapping("/events/{id}/edit")
    public String editEventForm(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long id,
                                Model model) {
        Event event = eventService.findEventById(id);
        Study study = studyService.findStudyWithManagersByPath(path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(event);
        model.addAttribute(modelMapper.map(event, EventForm.class));
        return "event/update-form";
    }

    @PostMapping("/events/{id}/edit")
    public String updateEvent(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long id,
                              @Valid EventForm eventForm, Errors errors, Model model) {

        Study study = studyService.getStudyToUpdate(account, path);
        Event event = eventService.findEventById(id);
        eventForm.setEventType(event.getEventType());
        eventValidator.validateUpdateForm(eventForm, event, errors);
        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute(event);
            return "event/update-form";
        }
        eventService.updateEvent(event, eventForm);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    @DeleteMapping("/events/{id}")
    public String deleteEvent(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long id) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        Event event = eventService.findEventById(id);
        eventService.deleteEvent(event);
        return "redirect:/study/" + study.getEncodedPath() + "/events";
    }

    @PostMapping("/events/{id}/enroll")
    public String newEnrollEvent(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long id) {
        Event event = eventService.findEventById(id);
        Study study = studyService.findStudyToEnroll(path);
        eventService.newEnrollment(event, account);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    @PostMapping("/events/{id}/disenroll")
    public String cancelEnrollment(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long id) {
        Event event = eventService.findEventById(id);
        Study study = studyService.findStudyToEnroll(path);
        eventService.cancelEnrollment(event, account);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }
}
