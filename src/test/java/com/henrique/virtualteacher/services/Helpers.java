package com.henrique.virtualteacher.services;

import com.henrique.virtualteacher.entities.*;
import com.henrique.virtualteacher.models.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Helpers {

    private final static ModelMapper mapper = new ModelMapper();

    public static User createMockUser() {
        return createUser();
    }

    public static User createMockUser(int id) {
        User createdUser = createUser();
        createdUser.setId(id);
        return createdUser;
    }

    public static UserModel createMockUserModel(User user) {
       return mapUserToModel(user);
    }

    private static UserModel mapUserToModel(User user) {
        UserModel userModel = new UserModel();
        userModel.setAssignments(createMockAssignmentList());
        userModel.setEmail(user.getEmail());
        userModel.setFirstname(user.getFirstName());
        userModel.setLastname(user.getLastName());
        userModel.setRolesList(user.getRoles());
        return userModel;
    }

    public static UserUpdateModel mapUserToUpdateModel(User user) {
        UserUpdateModel updateModel = new UserUpdateModel();
        updateModel.setUsername(user.getEmail());
        updateModel.setFirstname(user.getFirstName());
        updateModel.setLastname(user.getLastName());
        return updateModel;
    }

    public static UserModel createMockUserModel() {
        User user = createUser();
        return createMockUserModel(user);
    }

    public static User createMockAdmin() {
        return createAdmin();
    }

    public static User createMockTeacher() {
        return createTeacher();
    }

    public static Wallet createMockWallet(User walletOwner) { return createUserWallet(walletOwner); }

    public static Wallet createMockWallet(User walletOwner, BigDecimal initialAmount) {
        Wallet wallet = createUserWallet(walletOwner);
        wallet.setBalance(initialAmount);
        return wallet;
    }

    public static Course createMockCourse() {
        return createCourse();
    }

    public static Course createMockCourse(EnumTopic topic) {
        Course course = createCourse();
        course.setTopic(topic);
        return course;
    }

    public static Course createMockCourse(User creator) {Course created = createCourse(); created.setCreator(creator); return created; }

    public static Lecture createMockLecture(Course course) {
        return createLecture(course);
    }

    public static Lecture createMockLecture() {
        return createLecture(createMockCourse());
    }

    public static Transaction createMockTransaction() {
        return createTransaction();
    }

    public static Transaction createMockTransaction(User sender, User recipient) {
        return createTransaction(sender, recipient);
    }

    public static TransactionModel createTransactionModel() {
        Transaction transaction = createTransaction();
        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setAmount(transaction.getAmount());
        transactionModel.setCreationTime(LocalDate.now());
        transactionModel.setStatus(TransactionStatus.COMPLETED);
        transactionModel.setPurchasedCourse(createCourse());
        transactionModel.setRecipientWallet(createMockWallet(createMockUser(21)));
        transactionModel.setSenderWallet(createMockWallet(createMockUser(99)));
        return transactionModel;
    }

    public static TransactionModel createTransactionModel(Transaction transaction) {
        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setSenderWallet(transaction.getSenderWallet());
        transactionModel.setRecipientWallet(transaction.getRecipientWallet());
        transactionModel.setStatus(TransactionStatus.COMPLETED);
        transactionModel.setAmount(transaction.getAmount());
        transactionModel.setCreationTime(LocalDate.now());
        transactionModel.setPurchasedCourse(transaction.getPurchasedCourse());
        return transactionModel;
    }

    public static NFT createMockCourseEnrollment() { return  createCourseEnrollment();}

    public static VerificationToken createMockVerificationToken() { return createVerificationToken();}

    public static VerificationToken createMockVerificationToken(User verifier) {
        return createVerificationToken(verifier);
    }

    public static VerificationTokenModel createVerificationTokenModel(User verifier) {
        VerificationTokenModel tokenModel =  mapper.map(createVerificationToken(), new TypeToken<VerificationTokenModel>() {}.getType());
        tokenModel.setVerifierId(verifier.getId());
        return tokenModel;
    }

    public static VerificationTokenModel createTransactionTokenModel(Transaction transaction) {
        VerificationToken token  = createTransactionVerificationToken(transaction);
        VerificationTokenModel model = new VerificationTokenModel();
        model.setToken(token.getToken());
        model.setId(token.getId());
        model.setVerifierId(token.getVerifier().getId());
        model.setExpirationTime(token.getExpirationTime());
        model.setTransactionId(token.getTransaction().getId());
        return model;
    }

    public static VerificationTokenModel createVerificationTokenModel() {
        return mapper.map(createVerificationToken(), new TypeToken<VerificationTokenModel>() {}.getType());
    }


    private static VerificationToken createVerificationToken(User verifier) {
        VerificationToken verificationToken = createVerificationToken();
        verificationToken.setVerifier(verifier);
        return verificationToken;
    }

    private static VerificationToken createVerificationToken() {
        VerificationToken token =  new VerificationToken(createUser());
        token.setId(1);
        return token;
    }

    private static VerificationToken createTransactionVerificationToken(Transaction transaction) {
        VerificationToken token = new VerificationToken(transaction);
        token.setId(1);
        return token;
    }

    public static NFT createMockCourseEnrollment(User enrolledUser) {
        NFT NFT = createCourseEnrollment();
        NFT.setOwner(enrolledUser);
        return NFT;
    }

    public static NFT createMockCourseEnrollment(Course courseToEnroll) {
        NFT NFT = createCourseEnrollment();
        NFT.setCourse(courseToEnroll);
        return NFT;
    }

    public static NFT createMockCourseEnrollment(User userToEnroll, Course course) {
        NFT NFT = createCourseEnrollment();
        NFT.setCourse(course);
        NFT.setOwner(userToEnroll);
        return NFT;
    }

    private static NFT createCourseEnrollment() {
        NFT NFT = new NFT();
        NFT.setCourse(createCourse());
        NFT.setCompleted(true);
        NFT.setId(1);
        NFT.setOwner(createUser());
        return NFT;
    }

    private static Transaction createTransaction(User sender, User recipient) {
        Transaction transaction = new Transaction();
        addTransactionBasicInfo(transaction);
        transaction.setRecipientWallet(createMockWallet(recipient));
        transaction.setSenderWallet(createMockWallet(sender));
        return transaction;
    }

    public static Transaction createTransaction(Wallet senderWallet, Wallet recipientWallet, BigDecimal amount) {
        Transaction transaction = new Transaction(senderWallet, recipientWallet, amount);
        transaction.setId(1);
        return transaction;
    }

    public static Transaction createDepositTransaction(Wallet wallet, BigDecimal amount) {
        User walletOwner = wallet.getOwner();
        Transaction transaction = new Transaction(wallet, amount);
        transaction.setId(1);
        return transaction;
    }

    private static void addTransactionBasicInfo(Transaction transaction) {
        transaction.setId(1);
        transaction.setCreationTime(LocalDate.now());
        transaction.setPurchasedCourse(createCourse());
        transaction.setAmount(BigDecimal.valueOf(15.99));
    }

    private static Transaction createTransaction(){
        Transaction transaction = new Transaction();
        addTransactionBasicInfo(transaction);
        transaction.setRecipientWallet(createMockWallet(createMockUser()));
        transaction.setSenderWallet(createMockWallet(createTeacher()));
        return transaction;
    }

    public static LectureModel createMockLectureModel() {
        LectureModel lectureModel = new LectureModel();
        Lecture lecture = createMockLecture();
        mapper.map(lecture, lectureModel);
        return lectureModel;
    }

    public static Assignment createMockPendingAssignment() {
        return createAssignment(Status.PENDING);
    }

    public static Assignment createMockGradedAssignment() {
        return createAssignment(Status.GRADED);
    }

    public static Assignment createMockGradedAssignment(User user) {
        Assignment assignment = createAssignment(Status.GRADED);
        assignment.setUser(user);
        return assignment;
    }

    public static Assignment createMockPendingAssignment(User user) {
        Assignment assignment = createAssignment(Status.PENDING);
        assignment.setUser(user);
        return assignment;
    }

    public static Comment createMockComment () {
        return createComment();
    }

    public static Rating createMockRating() {
        return createRating();
    }

    private static Rating createRating() {
        Rating rating = new Rating();
        rating.setCourse(createCourse());
        rating.setUser(createUser());
        rating.setRating(5);
        rating.setId(1);
        return rating;
    }

    public static CommentModel createMockCommentModel() {
        CommentModel commentModel = new CommentModel();
        mapper.map(createComment(), commentModel);
        return commentModel;
    }

    private static Comment createComment() {
        Comment comment = new Comment();
        comment.setCourse(createMockCourse());
        comment.setUser(createUser());
        comment.getUser().setId(21);
        comment.setContent("content");
        comment.setId(1);
        return comment;
    }

    public static CourseModel createMockCourseModel(String courseTitle) {
        Course course = createCourse();
        CourseModel courseModel = new CourseModel();
        mapper.map(course, courseModel);
        courseModel.setTitle(courseTitle);
        return courseModel;
    }

    public static List<User> createMockUserList() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 4 ; i++) {
            User user = createUser();
            user.setId(i);
            users.add(user);
        }
        return users;
    }

    public static List<NFT> createCourseEnrollmentList(User enrolledUser) {
        List<NFT> NFTCours = new ArrayList<>();
        for (int i = 1; i < 5 ; i++) {
            NFT current = createCourseEnrollment();
            current.setId(i);
            current.setOwner(enrolledUser);
            NFTCours.add(current);
        }
        return NFTCours;
    }

    public static List<VerificationToken> createMockTokenList(User verifier) {
        List<VerificationToken> tokenList = new ArrayList<>();
        for (int i = 0; i < 4 ; i++) {
            VerificationToken current = createVerificationToken(verifier);
            tokenList.add(current);
        }
        return tokenList;
    }

    public static List<VerificationTokenModel> createTokenModelList(List<VerificationToken> verificationTokens) {
        List<VerificationTokenModel> modelList = new ArrayList<>();

        for (int i = 0; i < verificationTokens.size() ; i++) {
            VerificationToken current = verificationTokens.get(i);
            VerificationTokenModel tokenModel = new VerificationTokenModel();
            tokenModel.setToken(current.getToken());
            tokenModel.setExpirationTime(tokenModel.getExpirationTime());
            tokenModel.setId(current.getId());
            tokenModel.setVerifierId(current.getVerifier().getId());
            modelList.add(tokenModel);
        }
        return modelList;
    }

    public static List<Rating> createMockRatingList() {
        List<Rating> ratings = new ArrayList<>();
        for (int i = 0; i < 5 ; i++) {
            ratings.add(createMockRating());
        }
        return ratings;
    }

    public static Set<Comment> createMockCommentModelList() {
        Set<Comment> commentModels = new HashSet<>();
        for (int i = 0; i < 4 ; i++) {
            commentModels.add(createMockComment());
        }
        return commentModels;
    }

    public static List<Course> createMockCourseList() {
        List<Course> mockCourseList = new ArrayList<>();
        for (int i = 0; i < 5 ; i++) {
            mockCourseList.add(createCourse());
        }
        return mockCourseList;
    }

    public static List<Transaction> createMockTransactionList(User sender, User recipient) {

        List<Transaction> mockTransactions = new ArrayList<>();
        for (int i = 0; i < 5 ; i++) {
            Transaction current = createTransaction(sender, recipient);
            mockTransactions.add(current);
        }
        return mockTransactions;
    }

    public static List<Lecture> createMockLectureList(Course course){
        List<Lecture> mockLectureList = new ArrayList<>();
        int entryId = 1;
        for (int i = 0; i < 4 ; i++) {
            Lecture current = createLecture(course);
            current.setEntryId(entryId++);
            mockLectureList.add(current);;
        }
        return mockLectureList;
    }

    public static List<Assignment> createMockAssignmentList(User student) {
       return createMockAssignmentList().stream()
               .peek(assignment -> assignment.setUser(student))
               .collect(Collectors.toList());
    }

    public static List<Assignment> createGradedAssignmentList(User student) {
        return createMockAssignmentList().stream()
                .peek(assignment -> assignment.setStatus(Status.GRADED))
                .collect(Collectors.toList());
    }

    public static List<Assignment> createGradedAssignmentList(Course course, User student) {
        int indexCurrentCourseLecture = 0;
        return createMockAssignmentList().stream()
                .peek(assignment -> assignment.setLecture(course.getCourseLectures().get(indexCurrentCourseLecture)))
                .peek(assignment -> assignment.setUser(student))
                .collect(Collectors.toList());
    }

    public static List<Assignment> createPendingAssignmentList(User student) {
        return createMockAssignmentList().stream()
                .peek(assignment -> assignment.setUser(student))
                .collect(Collectors.toList());
    }

    public static List<Assignment> createMockAssignmentList(){
        List<Assignment> assignments = new ArrayList<>();
        for (int i = 0; i < 5 ; i++) {
            assignments.add(createAssignment(Status.GRADED));
        }
        return assignments;
    }

    public static User createUserFromRegisterModel(RegisterUserModel registerUserModel) {
        User user = new User();
        mapper.map(registerUserModel, user);
        return user;
    }

    private static Assignment createAssignment(Status status) {
        Assignment assignment = new Assignment();
        assignment.setId(1);
        assignment.setContent("content");
        assignment.setStatus(status);
        assignment.setLecture(createLecture());
        int grade = status == Status.GRADED ? 70 : 0;
        assignment.setGrade(grade);
        assignment.setUser(createUser());
        return assignment;
    }

    private static Lecture createLecture(Course course) {
        Lecture lecture =  new Lecture();
        lecture.setId(1);
        lecture.setCourse(course);
        lecture.setTitle("title");
        lecture.setDescription("description");
        lecture.setAssignmentText("do a megatask");
        lecture.setEntryId(1);
        lecture.setUsersCompleted(createMockUserList());
        lecture.setEnabled(true);
        lecture.setVideoLink("http//goHome.com");
        return lecture;
    }

    private static Lecture createLecture() {
        Lecture lecture =  new Lecture();
        lecture.setId(1);
        lecture.setCourse(createCourse());
        lecture.setTitle("title");
        lecture.setDescription("description");
        lecture.setAssignmentText("do a megatask");
        lecture.setEntryId(1);
        lecture.setEnabled(true);
        lecture.setUsersCompleted(createMockUserList());
        lecture.setVideoLink("http//goHome.com");
        return lecture;
    }

    private static Course createCourse() {
        Course course = new Course();
        course.setId(1);
        course.setTitle("info");
        course.setCreator(createTeacher());
        course.setDescription("description");
        course.setDifficulty(EnumDifficulty.INTERMEDIATE);
        course.setMintPrice(BigDecimal.valueOf(15.99));
        course.setNfts(new ArrayList<>());
        course.setRatings(new ArrayList<>());
        course.setTopic(EnumTopic.JAVA);
        course.setEnabled(true);
        course.setCourseLectures(createMockLectureList(course));
        course.setStartingDate(LocalDate.now());
        course.setSkill2("skill2");
        course.setSkill1("skill1");
        course.setSkill3("skill3");
        return course;
    }

    public static RegisterUserModel createUserRegisterModel() {
        return createRegisterModel();
    }

    private static RegisterUserModel createRegisterModel() {
    RegisterUserModel registerModel = new RegisterUserModel();
    registerModel.setFirstName("test");
    registerModel.setLastName("test");
    registerModel.setPassword("Test123@");
    registerModel.setPasswordConfirm("Test123@");
    registerModel.setEmail("test123@gmai.com");
    return registerModel;
    }


    private static Wallet createUserWallet(User walletOwner){
        Wallet wallet =  new Wallet(walletOwner);
        wallet.setId(walletOwner.getId());
        return wallet;
    }

    private static User createTeacher() {
        User teacher = new User();
        addBasicInfo(teacher);
        teacher.setId(2);
        teacher.setRoles(List.of(new Role(2, EnumRoles.TEACHER), new Role(1, EnumRoles.STUDENT)));

        return teacher;
    }

    private static User createAdmin() {
        User admin = new User();
        addBasicInfo(admin);
        admin.setRoles(List.of(new Role(2,EnumRoles.TEACHER), new Role(3, EnumRoles.ADMIN), new Role(1, EnumRoles.STUDENT)));

        return admin;
    }

    private static User createUser() {
        User user = new User();
        addBasicInfo(user);
        user.setNftCourses(new ArrayList<>());
        user.setAssignments(new ArrayList<>());
        user.setCompletedLectures(new HashSet<>());
        user.setRoles(List.of(new Role(1,EnumRoles.STUDENT)));

        return user;
    }

    private static void addBasicInfo(User user) {
        user.setId(1);
        user.setFirstName("test");
        user.setLastName("test");
        user.setPassword("123123@El");
        user.setEmail("test@gmail.com");
        user.setEnabled(true);
    }



}
