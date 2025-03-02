package com.blessing.rentaldream.modules.account.service;

import com.blessing.rentaldream.infra.file.FileManager;
import com.blessing.rentaldream.modules.account.UserAccount;
import com.blessing.rentaldream.modules.account.domain.Account;
import com.blessing.rentaldream.modules.account.domain.AccountTag;
import com.blessing.rentaldream.modules.account.domain.AccountZone;
import com.blessing.rentaldream.modules.account.form.PasswordForm;
import com.blessing.rentaldream.modules.account.form.ProfileForm;
import com.blessing.rentaldream.modules.account.form.SignUpForm;
import com.blessing.rentaldream.modules.account.repository.AccountRepository;
import com.blessing.rentaldream.modules.account.repository.AccountTagRepository;
import com.blessing.rentaldream.modules.account.repository.AccountZoneRepository;
import com.blessing.rentaldream.modules.post.PostService;
import com.blessing.rentaldream.modules.post.form.PostForm;
import com.blessing.rentaldream.modules.post.repository.PostRepository;
import com.blessing.rentaldream.modules.tag.Tag;
import com.blessing.rentaldream.modules.zone.Zone;
import com.blessing.rentaldream.modules.zone.ZoneForm;
import com.blessing.rentaldream.modules.zone.ZoneService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
@RequiredArgsConstructor

public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final ZoneService zoneService;
    private final AccountTagRepository accountTagRepository;
    private final AccountZoneRepository accountZoneRepository;
    private final PostService postService;
    private final ModelMapper modelMapper;
    private final PostRepository postRepository;

    @PostConstruct
    public void initAccount(){
       if(accountRepository.count() == 0){
           SignUpForm signUpForm = new SignUpForm();
           signUpForm.setNickname("blessing333");
           signUpForm.setEmail("blessing_333@naver.com");
           signUpForm.setPassword("qwerasdf");
           signUpForm.setConfirmPassword("qwerasdf");
           processSignUp(signUpForm);
           SignUpForm signUpForm1 = new SignUpForm();
           signUpForm1.setNickname("dlalswotl");
           signUpForm1.setEmail("dlalswotl@naver.com");
           signUpForm1.setPassword("qwerasdf");
           signUpForm1.setConfirmPassword("qwerasdf");
           processSignUp(signUpForm1);
       }

        if(postRepository.count() == 0){
            Account account = accountRepository.findByEmail("blessing_333@naver.com");
            String filePath = "/assets/thumbnails/";
            for (int i = 0; i < 5000; i++) {
                String fileName = "notebook.jpg";
                PostForm postForm = new PostForm();
                postForm.setTitle("노트북 " +i);
                postForm.setDescription("discr");
                postForm.setPrice(1000);
                postForm.setTagsWithJsonString("[{\"tagName\":\"노트북\"}]");
                postForm.setZonesWithJsonString("[{\"zoneName\":\"Seongnam(성남시)/Gyeonggi\"}]");
                postService.addTestPost(account,postForm,filePath+fileName,fileName);
            }

            for (int i = 0; i < 3000; i++) {
                String fileName = "cloth.jpg";
                PostForm postForm = new PostForm();
                postForm.setTitle("옷 " +i);
                postForm.setDescription("discr");
                postForm.setPrice(1000);
                postForm.setTagsWithJsonString("[{\"tagName\":\"옷\"}]");
                postForm.setZonesWithJsonString("[{\"zoneName\":\"Seongnam(성남시)/Gyeonggi\"}]");
                postService.addTestPost(account,postForm,filePath+fileName,fileName);
            }

            for (int i = 0; i < 2000; i++) {
                String fileName = "macbook.jpg";
                PostForm postForm = new PostForm();
                postForm.setTitle("맥북 " +i);
                postForm.setDescription("discr");
                postForm.setPrice(1000);
                postForm.setTagsWithJsonString("[{\"tagName\":\"노트북\"},{\"tagName\":\"맥북\"}]");
                postForm.setZonesWithJsonString("[{\"zoneName\":\"Seongnam(성남시)/Gyeonggi\"}]");
                postService.addTestPost(account,postForm,filePath+fileName,fileName);
            }
        }
    }

    public Account processSignUp(SignUpForm signUpForm){
        String encodedPassword = passwordEncoder.encode(signUpForm.getPassword());
        Account newAccount = Account.createNewAccount(signUpForm.getNickname(), signUpForm.getEmail(),encodedPassword);
        accountRepository.save(newAccount);
        return newAccount;
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(userEmail);
        if (account == null) {
            throw new UsernameNotFoundException(userEmail);
        }
        return new UserAccount(account);
    }

    public void addTag(Long accountId, Tag tag){
        Optional<Account> findAccount = accountRepository.findById(accountId);
        findAccount.ifPresent(account ->{
            AccountTag accountTag = AccountTag.createNewAccountTag(account,tag);
            account.addNewAccountTag(accountTag);
        });
    }

    public void deleteTag(Long accountId, Tag tag) {
        Optional<Account> findAccount = accountRepository.findById(accountId);
        findAccount.ifPresent(account ->{
            AccountTag accountTag = accountTagRepository.findByAccountAndTag(account,tag);
            account.deleteAccountTag(accountTag);
        });
    }

    public List<String> getTagNameList(Long accountId) {
        Optional<Account> findAccount = accountRepository.findById(accountId);
        List<String> list = new ArrayList<>();
        findAccount.ifPresent(account ->{
            account.getAccountTags().forEach(accountTag -> {
                list.add(accountTag.getTag().getTagName());
            });
        });
        return list;
    }

    public List<String> getZoneList(Long accountId) {
        Optional<Account> findAccount = accountRepository.findById(accountId);
        List<String> list = new ArrayList<>();
        findAccount.ifPresent(account ->{
            account.getAccountZones().forEach(accountZone -> {
                list.add(accountZone.getZone().toString());
            });
        });
        return list;
    }

    public void addZone(Long accountId,ZoneForm zoneForm) {
        Optional<Account> foundAccount = accountRepository.findById(accountId);
        Zone zone = zoneService.findByCityAndProvince(zoneForm.getCity(),zoneForm.getProvince());
        foundAccount.ifPresent(account -> {
            AccountZone newAccountZone = AccountZone.createNewAccountZone(account, zone);
            account.addNewAccountZone(newAccountZone);
        });
    }

    public void removeZone(Long id, Zone zone) {
        Optional<Account> foundAccount = accountRepository.findById(id);
        foundAccount.ifPresent(account -> {
            AccountZone accountZone = accountZoneRepository.findByAccountAndZone(account,zone);
            account.deleteAccountZone(accountZone);
        });
    }

    public void updateProfile(Long accountId, ProfileForm profileForm) {
        Optional<Account> foundAccount = accountRepository.findById(accountId);
        foundAccount.ifPresent(account -> {
            account.updateProfile(profileForm);
        });
    }

    public void changeAccountPassword(Long accountId, PasswordForm passwordForm) {
        Optional<Account> foundAccount = accountRepository.findById(accountId);
        foundAccount.ifPresent(account -> {
            String encodedPassword = passwordEncoder.encode(passwordForm.getNewPassword());
            account.changePassword(encodedPassword);
        });
    }

    public boolean checkValidAccountById(Long accountId){
        return accountRepository.existsById(accountId);
    }
    public Optional<Account> findAccountById(Long accountId){
        return accountRepository.findById(accountId);
    }
}
