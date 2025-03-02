package com.blessing.rentaldream.modules.account.controller;

import com.blessing.rentaldream.modules.account.CurrentUser;
import com.blessing.rentaldream.modules.account.domain.Account;
import com.blessing.rentaldream.modules.account.form.PasswordForm;
import com.blessing.rentaldream.modules.account.form.ProfileForm;
import com.blessing.rentaldream.modules.account.repository.AccountRepository;
import com.blessing.rentaldream.modules.account.service.AccountService;
import com.blessing.rentaldream.modules.account.validator.PasswordFormValidator;
import com.blessing.rentaldream.modules.account.validator.ProfileFormValidator;
import com.blessing.rentaldream.modules.tag.Tag;
import com.blessing.rentaldream.modules.tag.TagForm;
import com.blessing.rentaldream.modules.tag.TagService;
import com.blessing.rentaldream.modules.zone.Zone;
import com.blessing.rentaldream.modules.zone.ZoneForm;
import com.blessing.rentaldream.modules.zone.ZoneService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

import static com.blessing.rentaldream.infra.config.UrlConfig.*;
import static com.blessing.rentaldream.infra.config.ViewNameConfig.*;

@Controller
@RequiredArgsConstructor
public class SettingController {
    private final ProfileFormValidator profileFormValidator;
    private final ObjectMapper objectMapper;
    private final TagService tagService;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final ZoneService zoneService;
    private final ModelMapper modelMapper;
    private final PasswordFormValidator passwordFormValidator;
    private static final String PASSWORD_CHANGE_MESSAGE = "비밀번호가 변경되었습니다.";

    @InitBinder("profileForm")
    public void initProfileFormBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(profileFormValidator);
    }

    @InitBinder("passwordForm")
    public void initPasswordFormBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(passwordFormValidator);
    }

    @GetMapping(ACCOUNT_SETTING_PROFILE_URL)
    public String createAccountProfileView(@CurrentUser Account account,Model model){
        Account foundAccount = accountRepository.findById(account.getId()).get();
        ProfileForm profileForm = modelMapper.map(foundAccount, ProfileForm.class);
        model.addAttribute("account",foundAccount);
        model.addAttribute(profileForm);
        return ACCOUNT_SETTING_PROFILE_VIEW;
    }

    @PostMapping(ACCOUNT_SETTING_PROFILE_URL)
    public String editAccountProfile(@CurrentUser Account account, @Valid @ModelAttribute ProfileForm profileForm, Errors errors, Model model){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return ACCOUNT_SETTING_PROFILE_VIEW;
        }
        accountService.updateProfile(account.getId(),profileForm);
        return REDIRECT_URL + ACCOUNT_SETTING_PROFILE_URL;
    }

    @GetMapping(ACCOUNT_SETTING_TAG_URL)
    public String createAccountTagSettingView(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        List<String> tagNameList = accountService.getTagNameList(account.getId());
        model.addAttribute("tags", tagNameList);
        String jsonString = tagService.findAllTagAsJsonString();
        model.addAttribute("recommendTagList",jsonString);
        return ACCOUNT_SETTING_TAG_VIEW;
    }

    @PostMapping(ACCOUNT_SETTING_TAG_URL)
    public ResponseEntity addTag(@CurrentUser Account account, @RequestBody TagForm tagForm){
        Tag tag = tagService.addNewTag(tagForm.getTagName());
        accountService.addTag(account.getId(),tag);
        return ResponseEntity.ok().build();

    }

    @DeleteMapping(ACCOUNT_SETTING_TAG_URL)
    public ResponseEntity deleteTag(@CurrentUser Account account, @RequestBody TagForm tagForm){
        String tagName = tagForm.getTagName();
        Tag tag = tagService.findByTagName(tagName);
        if(tag == null){
            return ResponseEntity.badRequest().build();
        }
        accountService.deleteTag(account.getId(),tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping(ACCOUNT_SETTING_ZONE_URL)
    public String createAccountZoneView(@CurrentUser Account account,Model model) throws JsonProcessingException {
        model.addAttribute(account);
        List<String> zoneList = accountService.getZoneList(account.getId());
        model.addAttribute("zones", zoneList);
        String jsonString = zoneService.findAllZonesAsJsonString();
        model.addAttribute("cityList", jsonString);
        return ACCOUNT_SETTING_ZONE_VIEW;
    }

    @PostMapping(ACCOUNT_SETTING_ZONE_URL)
    public ResponseEntity addAccountZone(@CurrentUser Account account, @RequestBody ZoneForm zoneForm){
        accountService.addZone(account.getId(), zoneForm);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(ACCOUNT_SETTING_ZONE_URL)
    public ResponseEntity removeAccountZone(@CurrentUser Account account, @RequestBody ZoneForm zoneForm){
        Zone zone = zoneService.findByCityAndProvince(zoneForm.getCity(),zoneForm.getProvince());
        if(zone == null){
            return ResponseEntity.badRequest().build();
        }
        accountService.removeZone(account.getId(), zone);
        return ResponseEntity.ok().build();
    }
    @GetMapping(ACCOUNT_SETTING_PASSWORD_URL)
    public String createPasswordModifyView(@CurrentUser Account account,Model model){
        PasswordForm passwordForm = new PasswordForm();
        model.addAttribute(account);
        model.addAttribute(passwordForm);
        return ACCOUNT_SETTING_PASSWORD_VIEW;
    }

    @PostMapping(ACCOUNT_SETTING_PASSWORD_URL)
    public String passwordModify(@CurrentUser Account account, @Valid @ModelAttribute PasswordForm passwordForm, Errors errors, Model model, RedirectAttributes redirectAttributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return ACCOUNT_SETTING_PASSWORD_VIEW;
        }
        accountService.changeAccountPassword(account.getId(),passwordForm);
        redirectAttributes.addFlashAttribute("message",PASSWORD_CHANGE_MESSAGE);
        return REDIRECT_URL + ACCOUNT_SETTING_PASSWORD_URL;
    }
}
