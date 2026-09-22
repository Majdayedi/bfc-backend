package bfc.bfc.config;

import bfc.bfc.entities.TeamMember;
import bfc.bfc.entities.TeamMemberRole;
import bfc.bfc.entities.ExtraFlag;
import bfc.bfc.entities.User;
import bfc.bfc.entities.Article;
import bfc.bfc.entities.Course;
import bfc.bfc.repository.TeamMemberRepository;
import bfc.bfc.repository.UserRepository;
import bfc.bfc.repository.ArticleRepository;
import bfc.bfc.repository.ProjectRepository;
import bfc.bfc.entities.Project;
import bfc.bfc.entities.HistoryEvent;
import bfc.bfc.repositories.CourseRepository;
import bfc.bfc.repositories.HistoryEventRepository;
import bfc.bfc.entities.Representative;
import bfc.bfc.repositories.RepresentativeRepository;
import bfc.bfc.entities.ServicePage;
import bfc.bfc.repositories.ServicePageRepository;
import bfc.bfc.entities.JourneyStep;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ArticleRepository articleRepository;
    private final CourseRepository courseRepository;
    private final ProjectRepository projectRepository;
    private final HistoryEventRepository historyEventRepository;
    private final RepresentativeRepository representativeRepository;
    private final ServicePageRepository servicePageRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, 
                      TeamMemberRepository teamMemberRepository, 
                      ArticleRepository articleRepository,
                      CourseRepository courseRepository,
                      ProjectRepository projectRepository,
                      HistoryEventRepository historyEventRepository,
                      RepresentativeRepository representativeRepository,
                      ServicePageRepository servicePageRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.articleRepository = articleRepository;
        this.courseRepository = courseRepository;
        this.projectRepository = projectRepository;
        this.historyEventRepository = historyEventRepository;
        this.representativeRepository = representativeRepository;
        this.servicePageRepository = servicePageRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Seed default admin user
        if (!userRepository.existsByEmail("admin@bfc.com")) {
            User admin = User.builder()
                    .name("Admin")
                    .email("admin@bfc.com")
                    .password(passwordEncoder.encode("admin123"))
                    .build();

            userRepository.save(admin);
            log.info("Default admin user seeded (admin@bfc.com / admin123)");
        } else {
            log.info("Admin user already exists, skipping seed");
        }

        // Seed team members from About Us page
        if (teamMemberRepository.count() == 0) {
            seedTeamMembers();
            log.info("Team members seeded successfully");
        } else {
            log.info("Team members already exist, skipping seed");
        }

        // Seed articles
        if (articleRepository.count() == 0) {
            seedArticles();
            log.info("Articles seeded successfully");
        } else {
            log.info("Articles already exist, skipping seed");
        }

        // Seed projects
        if (projectRepository.count() == 0) {
            seedProjects();
            log.info("Projects seeded successfully");
        } else {
            log.info("Projects already exist, skipping seed");
        }

        // Seed service pages (only if empty)
        if (servicePageRepository.count() == 0) {
            seedServicePages();
            log.info("Service pages seeded successfully");
        } else {
            log.info("Service pages already exist, skipping seed");
        }

        // Seed courses / certifications (forced clear and seed for testing)
        courseRepository.deleteAll();
        seedCourses();
        log.info("Courses seeded successfully");

        // Seed history events (forced clear and seed for testing)
        historyEventRepository.deleteAll();
        seedHistoryEvents();
        log.info("History events re-seeded successfully");

        // Seed representatives
        representativeRepository.deleteAll();
        seedRepresentatives();
        log.info("Representatives re-seeded successfully");
    }

    private void seedHistoryEvents() {
        List<HistoryEvent> events = List.of(
            HistoryEvent.builder()
                .eventYear(2010)
                .title("MGI BFC")
                .description("MGI BFC is the parent entity. It is an accounting and audit firm founded in 2010 and based in Tunis. MGI BFC is a member of the international MGI WORLDWIDE network, one of the top 20 global consulting and audit networks.::link=/representatives/tunisia")
                .backgroundColor("#f8f9ff")
                .countryFlag("https://flagcdn.com/w80/tn.png")
                .logoUrl("/src/assets/MGI-BFC.png")
                .photoUrl("/src/assets/history/tunisia.png")
                .build(),
            HistoryEvent.builder()
                .eventYear(2020)
                .title("BFC International & Academy")
                .description("Founded in 2020, BFC International & Academy is a consulting and training firm. As a partner of IRM and ICI in Africa, it also provides outsourcing services in France and Canada.::link=/representatives/tunisia")
                .backgroundColor("#edf4ff")
                .countryFlag("https://flagcdn.com/w80/tn.png")
                .logoUrl("/src/assets/bfc.jpg")
                .photoUrl("/src/assets/history/tunisia2.png")
                .build()
        );
        historyEventRepository.saveAll(events);
    }

    private void seedRepresentatives() {
        List<TeamMember> teamMembers = teamMemberRepository.findAll();

        TeamMember nadia    = teamMembers.stream().filter(m -> m.getName().contains("Nadia")).findFirst().orElse(null);
        TeamMember ines     = teamMembers.stream().filter(m -> m.getName().contains("Ines")).findFirst().orElse(null);
        TeamMember majd     = teamMembers.stream().filter(m -> m.getName().contains("Majd")).findFirst().orElse(null);
        TeamMember medAmine = teamMembers.stream().filter(m -> m.getName().contains("Mohamed Amine")).findFirst().orElse(null);
        TeamMember tasnim   = teamMembers.stream().filter(m -> m.getName().contains("Tasnim")).findFirst().orElse(null);

        List<Representative> reps = List.of(
            Representative.builder()
                .slug("congo")
                .title("BFC Congo")
                .subtitle("Your partner in Central Africa")
                .creationYear(2023)
                .description("BFC Congo supports you through tailored solutions combining regional nuances with international standard consulting. We help businesses navigate the dynamic economy of the Congo Basin.")
                .location("Brazzaville, Republic of Congo")
                .manager(nadia)
                .globeMarkerTop("52%")
                .globeMarkerLeft("53%")
                .globeViewRotateY("148deg")
                .globeViewMapX("58%")
                .flagIconUrl("https://flagcdn.com/w80/cg.png")
                .imageUrl("/uploads/offices/bfc_congo.png")
                .projectCountries(List.of("Republic of Congo", "Congo", "Congo rdc"))
                .fallbackCountries(List.of("Cameroon", "Ivory Coast"))
                .build(),
            Representative.builder()
                .slug("senegal")
                .title("BFC Senegal")
                .subtitle("Influence in West Africa")
                .creationYear(2022)
                .description("Located in the heart of West Africa, BFC Senegal is dedicated to business transformation and institutional capacity building through innovative strategies.")
                .location("Dakar, Senegal")
                .manager(ines)
                .globeMarkerTop("43%")
                .globeMarkerLeft("46%")
                .globeViewRotateY("140deg")
                .globeViewMapX("55%")
                .flagIconUrl("https://flagcdn.com/w80/sn.png")
                .imageUrl("/uploads/offices/bfc_senegal.png")
                .projectCountries(List.of("Senegal"))
                .fallbackCountries(List.of("Benin", "Guinea", "Niger", "Mali", "Ivory Coast"))
                .build(),
            Representative.builder()
                .slug("tunisia")
                .title("BFC Tunisia")
                .subtitle("The bridge between Africa and Europe")
                .creationYear(2020)
                .description("BFC Tunisia operates as a strategic hub offering high-level consulting by leveraging exceptional human capital and mastery of North African markets.")
                .location("Tunis, Tunisia")
                .manager(majd)
                .globeMarkerTop("32%")
                .globeMarkerLeft("40%")
                .globeViewRotateY("165deg")
                .globeViewMapX("63%")
                .flagIconUrl("https://flagcdn.com/w80/tn.png")
                .imageUrl("/uploads/offices/bfc_tunisia.jpg")
                .projectCountries(List.of("Tunisia"))
                .fallbackCountries(List.of())
                .build(),
            Representative.builder()
                .slug("guinea")
                .title("BFC Guinea")
                .subtitle("Expertise driving growth")
                .creationYear(2022)
                .description("Our firm is committed to providing pragmatic solutions and tailored support to businesses and institutions in Guinea for sustainable growth.")
                .location("Conakry, Guinea")
                .manager(medAmine)
                .globeMarkerTop("46%")
                .globeMarkerLeft("47%")
                .globeViewRotateY("145deg")
                .globeViewMapX("56%")
                .flagIconUrl("https://flagcdn.com/w80/gn.png")
                .imageUrl("/uploads/offices/bfc_guinee.jpeg")
                .projectCountries(List.of("Guinea"))
                .fallbackCountries(List.of())
                .build(),
            Representative.builder()
                .slug("mauritania")
                .title("BFC Mauritania")
                .subtitle("Strategic support and development")
                .creationYear(2025)
                .description("BFC continues its expansion with a strengthened presence, developing new local partnerships to address your economic and structural challenges.")
                .location("Nouakchott, Mauritania")
                .manager(tasnim)
                .globeMarkerTop("42%")
                .globeMarkerLeft("46%")
                .globeViewRotateY("142deg")
                .globeViewMapX("54%")
                .flagIconUrl("https://flagcdn.com/w80/mr.png")
                .imageUrl("/uploads/offices/bfc_mauritania.png")
                .projectCountries(List.of("Mauritania"))
                .fallbackCountries(List.of())
                .build()
        );
        representativeRepository.saveAll(reps);

        // Auto-generate History Events for specific representatives (matching frontend workflow)
        for (Representative rep : reps) {
            if (rep.getSlug().equals("guinea") || rep.getSlug().equals("senegal") || rep.getSlug().equals("congo")) {
                String baseDesc = "";
                String bgColor = "";
                String photoUrl = "";
                if (rep.getSlug().equals("guinea")) {
                    baseDesc = "Our expansion began with the launch of BFC Guinea in " + rep.getCreationYear() + ". This entity was created to serve the sub-region and ensure closer expert support to meet client needs.";
                    bgColor = "#ecf7f0";
                    photoUrl = "/src/assets/history/guinee.png";
                } else if (rep.getSlug().equals("senegal")) {
                    baseDesc = "BFC Senegal further strengthened our presence in West Africa. The firm offers a wide range of services related to IT, management, training, and organizational development.";
                    bgColor = "#fdf6eb";
                    photoUrl = "/src/assets/history/senegal.png";
                } else if (rep.getSlug().equals("congo")) {
                    baseDesc = "BFC expanded its footprint into the Congo Basin. The firm entered Central Africa by delivering high-level consulting and training services.";
                    bgColor = "#ebf0fc";
                    photoUrl = "/src/assets/history/congo.png";
                }

                String managerEmail = rep.getManager() != null && rep.getManager().getEmail() != null ? rep.getManager().getEmail() : "";
                String managerName = rep.getManager() != null && rep.getManager().getName() != null ? rep.getManager().getName() : "";
                String encodedDesc = baseDesc + "::link=/representatives/" + rep.getSlug() + "::email=" + managerEmail + "::managerName=" + managerName;

                HistoryEvent event = HistoryEvent.builder()
                        .eventYear(rep.getCreationYear() != null ? rep.getCreationYear() : 2022)
                        .title(rep.getTitle())
                        .description(encodedDesc)
                        .backgroundColor(bgColor)
                        .countryFlag(rep.getFlagIconUrl())
                        .logoUrl(rep.getImageUrl())
                        .photoUrl(photoUrl)
                        .build();
                historyEventRepository.save(event);
            }
        }
    }


    private void seedTeamMembers() {
        TeamMember[] members = new TeamMember[] {
            TeamMember.builder()
                .name("Nadia Yaich")
                .role("CEO & Country Manager Congo")
                .img("/uploads/team/nadia.jpeg")
                .email("nadia.yaich@bfc.com.tn")
                .phone("+216-58-422-199")
                .cvUrl("/uploads/cv/CV Nadia YAICH  F\u00e9vrier 2026.pdf")
                .countryName("Republic of the Congo")
                .countryFlagUrl("https://flagcdn.com/w80/cg.png")
                .displayOrder(1)
                .showPrimaryFlag(true)
                .roleType(TeamMemberRole.CEO)
                .build(),

            TeamMember.builder()
                .name("Mohamed Amine Sahli")
                .role("Associate & Country Manager Guinea")
                .img("/uploads/team/medamine.jpeg")
                .email("mohamedamine.sahli@bfc.com.tn")
                .phone("+216 98 747 836 / +224 623 27 30 73")
                .cvUrl("/uploads/cv/CV Mohamed Amine Sahli (2).pdf")
                .countryName("Guinea")
                .countryFlagUrl("https://flagcdn.com/w80/gn.png")
                .displayOrder(2)
                .showPrimaryFlag(true)
                .roleType(TeamMemberRole.ASSOCIATE)
                .build(),

            TeamMember.builder()
                .name("Ines Yaich")
                .role("Country Manager BFC Senegal")
                .img("/uploads/team/ines.jpeg")
                .email("ines.yaich@bfc.com.tn")
                .phone("Phone not provided")
                .cvUrl(null)
                .countryName("Senegal")
                .countryFlagUrl("https://flagcdn.com/w80/sn.png")
                .displayOrder(3)
                .showPrimaryFlag(true)
                .roleType(TeamMemberRole.COUNTRY_MANAGER)
                .build(),

            TeamMember.builder()
                .name("Tasnim Zouaoui")
                .role("Country Manager Mauritania & Mali")
                .img("/uploads/team/tasnim.jpeg")
                .email("tasnim.zouaoui@bfc.com.tn")
                .phone("+216-98-194-202")
                .cvUrl("/uploads/cv/CV Tasnim Zouaoui .pdf")
                .countryName("Mauritania")
                .countryFlagUrl("https://flagcdn.com/w80/mr.png")
                .displayOrder(4)
                .showPrimaryFlag(true)
                .roleType(TeamMemberRole.COUNTRY_MANAGER)
                .extraFlags(List.of(new ExtraFlag("Mali", "https://flagcdn.com/w80/ml.png")))
                .build(),

            TeamMember.builder()
                .name("Zeineb Sboui")
                .role("Consultant")
                .img("/uploads/team/zeineb.jpeg")
                .email("zeineb.sboui@bfc.com.tn")
                .phone("+216-98-135-930")
                .cvUrl("/uploads/cv/CV ZEINEB SBOUI (2).pdf")
                .countryName("Tunisia")
                .countryFlagUrl("https://flagcdn.com/w80/tn.png")
                .displayOrder(5)
                .showPrimaryFlag(false)
                .roleType(TeamMemberRole.CONSULTANT)
                .build(),

            TeamMember.builder()
                .name("Chaima Gader")
                .role("Auditing Accountant")
                .img("/uploads/team/chaima.jpeg")
                .email("chaima.gader@bfc.com.tn")
                .phone("+216-98-747-842")
                .cvUrl(null)
                .countryName("Tunisia")
                .countryFlagUrl("https://flagcdn.com/w80/tn.png")
                .displayOrder(6)
                .showPrimaryFlag(true)
                .roleType(TeamMemberRole.AUDITING_ACCOUNTANT)
                .build(),
        };

        for (TeamMember member : members) {
            teamMemberRepository.save(member);
        }
    }

    private void seedArticles() {
        Article[] articles = new Article[] {
            Article.builder()
                .title("Digital Transformation in Africa and MENA: Why Strategy, Not Technology, Determines Outcomes")
                .subtitle("Learn why successful digital transformation in Africa depends on strategy, governance, and trust infrastructure—not just technology.")
                .slug("digitalization-strategy")
                .category("Strategy")
                .author("BFC Insights")
                .publishDate("March 15, 2025")
                .readingTime("8 min read")
                .summary("Learn why successful digital transformation in Africa depends on strategy, governance, and trust infrastructure—not just technology.")
                .heroImage("https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?auto=format&fit=crop&w=800")
                .contentJson(loadJsonFromResource("seed/digitalization-strategy.json"))
                .build(),

            Article.builder()
                .title("SME Formalization and Digitalization in Africa: A Strategic Lever for Growth, Tax Revenue, and Financial Inclusion")
                .subtitle("Discover how digitalization enables SME formalization, financial inclusion, and economic growth across Africa and MENA.")
                .slug("sme-formalization")
                .category("Policy")
                .author("BFC Insights")
                .publishDate("January 22, 2025")
                .readingTime("10 min read")
                .summary("Discover how digitalization enables SME formalization, financial inclusion, and economic growth across Africa and MENA.")
                .heroImage("https://images.unsplash.com/photo-1520607162513-77705c0f0d4a?auto=format&fit=crop&w=800")
                .contentJson(loadJsonFromResource("seed/sme-formalization.json"))
                .build(),

            Article.builder()
                .title("Why Timing Matters: The Cost of Delaying PKI Implementation In Africa")
                .subtitle("Delaying PKI implementation increases costs, complexity, and risks in national digital strategies.")
                .slug("pki-timing-matters")
                .category("Tech")
                .author("BFC Insights")
                .publishDate("February 10, 2025")
                .readingTime("9 min read")
                .summary("Delaying PKI implementation increases costs, complexity, and risks in national digital strategies. Learn why trust infrastructure is critical for digital economies in Africa and MENA.")
                .heroImage("https://images.unsplash.com/photo-1521737604893-d14cc237f11d?auto=format&fit=crop&w=800")
                .contentJson(loadJsonFromResource("seed/pki-timing-matters.json"))
                .build(),

            Article.builder()
                .title("Public Key Infrastructure (PKI) in Africa: The Strategic Backbone of Digital Trust, Sovereignty, and Scalable Services")
                .subtitle("Explore how Public Key Infrastructure (PKI) enables secure digital identity, trusted transactions, and scalable e-government systems across Africa.")
                .slug("pki-strategic-backbone")
                .category("Tech")
                .author("BFC Insights")
                .publishDate("April 5, 2025")
                .readingTime("11 min read")
                .summary("Explore how Public Key Infrastructure (PKI) enables secure digital identity, trusted transactions, and scalable e-government systems across Africa and MENA.")
                .heroImage("https://images.unsplash.com/photo-1504384308090-c894fdcc538d?auto=format&fit=crop&w=800")
                .contentJson(loadJsonFromResource("seed/pki-strategic-backbone.json"))
                .build()
        };

        for (Article article : articles) {
            articleRepository.save(article);
        }
    }

    private String loadJsonFromResource(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("Resource not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to load resource: {}", path, e);
            return "{}";
        }
    }

    private void seedProjects() {
        try {
            String json = loadJsonFromResource("seed/projects.json");
            ObjectMapper mapper = new ObjectMapper();
            List<Project> projects = mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<Project>>() {});
            for (Project p : projects) {
                if (p.getYear() != null && !p.getYear().trim().isEmpty()) {
                    String[] parts = p.getYear().split(" - ");
                    if (parts.length == 2) {
                        p.setStartDate(parts[0].trim());
                        p.setEndDate(parts[1].trim());
                    } else if (parts.length == 1) {
                        p.setStartDate(parts[0].trim());
                        p.setEndDate(parts[0].trim());
                    }
                }
                projectRepository.save(p);
            }
        } catch (Exception e) {
            log.error("Failed to seed projects: ", e);
        }
    }

    private void seedCourses() {
        Course[] courses = new Course[] {
            Course.builder()
                .title("Fundamentals of Risk Management (FoRM)")
                .institution("Institute of Risk Management (IRM) - London")
                .country("International")
                .year("2026")
                .category("International Courses")
                .topics("Risk concepts, assessment and treatment, risk appetite, risk transfer, business continuity, monitoring and review, risk policy.")
                .logo("/uploads/certif/IRM.png")
                .isAccredited(true)
                .programs("3 Days + Final Examination")
                .accreditation("IRM Official Certificate Program")
                .intake("2026")
                .description("Official certification training from the Institute of Risk Management of London focused on practical ERM implementation and business-aligned risk decision making.")
                .certificationDescription("Participants: Risk Managers, Internal Controllers, Internal Auditors, Administrators, Executives, Senior Managers, Department Heads. Certificate delivered by IRM upon passing final exam.")
                .brochureUrl("/pdfs/irm-form.pdf")
                .intro("Official FoRM certification by IRM London. The IRM is the world's leading organization in risk management. It helps build excellence in risk management to enhance how organizations operate. The IRM provides globally recognized qualifications and training, publishes research and informed leadership, and sets professional standards that define the knowledge, skills, and behaviors today's risk professionals need to meet the demands of an increasingly complex and challenging business environment. This course builds a practical enterprise risk management mindset and equips participants to deploy risk frameworks that are aligned with business strategy and governance expectations.")
                .participants("Risk Managers, Internal Controllers, Internal Auditors, Administrators, Executives, Senior Managers, Department Heads.")
                .duration("3 days + final exam")
                .location("International sessions")
                .language("English")
                .learnPoints(List.of(
                    "Understand risk and risk management fundamentals in organizational contexts.",
                    "Implement risk assessment, risk treatment, and risk register practices.",
                    "Define risk appetite, tolerance, and risk transfer mechanisms.",
                    "Embed risk culture, policy, monitoring, and review cycles.",
                    "Prepare for IRM final certification assessment."
                ))
                .journeySteps(List.of(
                    JourneyStep.builder().title("Journey 01 - Build Foundations").detail("Clarify risk principles, why risk management matters, and core ERM disciplines.").build(),
                    JourneyStep.builder().title("Journey 02 - Analyze and Prioritize Risks").detail("Apply assessment tools, risk profiling, consequence and probability matrices.").build(),
                    JourneyStep.builder().title("Journey 03 - Treat and Embed").detail("Design treatments, define appetite and tolerance, and integrate risk culture.").build(),
                    JourneyStep.builder().title("Journey 04 - Validate and Certify").detail("Consolidate knowledge and complete final FoRM exam preparation.").build()
                ))
                .build(),
            Course.builder()
                .title("Certified Internal Control Specialist (CICS)")
                .institution("Internal Control Institute (ICI) - USA")
                .country("International")
                .year("2026")
                .category("International Courses")
                .topics("Control environment, COSO components, risk evaluation, governance practices, reporting, internal control implementation and project steering.")
                .logo("/uploads/certif/ici.png")
                .isAccredited(true)
                .programs("5 Days + Final Examination")
                .accreditation("ICI Official Certification")
                .intake("2026")
                .description("Official international certifying program from ICI to design, implement, assess, and manage internal control systems with governance alignment.")
                .certificationDescription("Includes exam voucher, pre-assessment test, module tests, and training materials. The program is aimed at executives, directors, administrators, internal controllers, auditors, inspectors, GRC professionals, and risk managers.")
                .brochureUrl("/pdfs/cics.pdf")
                .intro("Official CICS program from the Internal Control Institute (ICI). The course focuses on control architecture, governance effectiveness, application of the COSO framework, and operational internal control implementation.    The Internal Control Institute™ (ICI)—the only global organization dedicated exclusively to internal control and corporate governance—offers an official international certification program for designing, implementing, assessing, and managing internal control systems aligned with governance, providing specialized methodologies, guidelines, and comprehensive controls for organizations.")
                .participants("Executives, Directors, Administrators, Internal Controllers, Internal Auditors, Inspectors, GRC professionals, Risk Managers.")
                .duration("5 days + final exam")
                .location("International cohorts")
                .language("English")
                .learnPoints(List.of(
                    "Design and structure enterprise internal control systems.",
                    "Develop control environment and control ownership across teams.",
                    "Evaluate control effectiveness and risk exposure using COSO components.",
                    "Implement reporting, communication, and governance review practices.",
                    "Lead internal control projects and change management programs."
                ))
                .journeySteps(List.of(
                    JourneyStep.builder().title("Journey 01 - Control Fundamentals").detail("Set the internal control baseline and map current control maturity.").build(),
                    JourneyStep.builder().title("Journey 02 - Risk-Control Alignment").detail("Connect risks to controls through COSO-based structuring.").build(),
                    JourneyStep.builder().title("Journey 03 - Governance and Reporting").detail("Strengthen communication flows and governance oversight.").build(),
                    JourneyStep.builder().title("Journey 04 - Certification Completion").detail("Finalize assessment readiness and pass ICI certification exam.").build()
                ))
                .build(),
            Course.builder()
                .title("Innovation Workshop: Designing Innovation")
                .institution("BFC Group")
                .country("Tunisia")
                .year("2026")
                .category("Our Courses")
                .topics("Innovation definition, strategic alignment, innovation horizons, innovation process, innovation tools, prioritization of initiatives.")
                .logo("/uploads/certif/bfc.png")
                .isAccredited(false)
                .programs("Interactive Workshop (6 Hours)")
                .accreditation("BFC Group Workshop")
                .intake("Tunis 2026")
                .description("Interactive and practical workshop to align strategy, horizons, and execution of innovation for organizations and leadership teams.")
                .certificationDescription("Expected outcomes: innovation-effort diagnosis, primary innovation register synthesis, and practical alignment between strategic goals and innovation types. ")
                .brochureUrl("/pdfs/innovation-workshop.pdf")
                .intro("An executive-focused workshop that clarifies innovation concepts, aligns innovation initiatives with strategic priorities, and translates innovation ambition into actionable execution tracks.")
                .participants("R&D team, Project/Product Managers, CEO, COO, Strategy/Development Director, Industrial/Plant Director, Production Manager, Quality/Certification Manager, Sales/Marketing Managers, Customer Relations Manager, Business Development Manager, Risk Manager.")
                .duration("6 hours")
                .location("On-site at client premises")
                .language("French")
                .learnPoints(List.of(
                    "Distinguish innovation from creativity, invention, and incremental improvement.",
                    "Align innovation efforts with strategic priorities and business goals.",
                    "Use horizons and innovation types for portfolio prioritization.",
                    "Apply practical innovation processes and tools.",
                    "Produce a first innovation diagnostic and a starter innovation register."
                ))
                .journeySteps(List.of(
                    JourneyStep.builder().title("Journey 01 - Clarify").detail("Build shared language and understanding of what innovation is and is not.").build(),
                    JourneyStep.builder().title("Journey 02 - Align").detail("Connect innovation initiatives to strategy, priorities, and operational needs.").build(),
                    JourneyStep.builder().title("Journey 03 - Structure").detail("Apply horizons, process, and tools to organize execution.").build(),
                    JourneyStep.builder().title("Journey 04 - Activate").detail("Deliver diagnostic output and a practical first innovation roadmap.").build()
                ))
                .build(),
            Course.builder()
                .title("Generative AI for Audit and Internal Control")
                .institution("BFC Academy & E2B Training")
                .country("Tunisia")
                .year("2026")
                .category("Our Courses")
                .topics("Prompt engineering, NotebookLM, Claude, Claude Cowork, risk analysis automation, compliance checks, security conflicts, dashboarding.")
                .logo("/uploads/certif/bfc.png")
                .isAccredited(false)
                .programs("4-Day Certifying Training + Final Test")
                .accreditation("BFC Academy Certification")
                .intake("Tunis 2026")
                .description("Certifying training to transform auditors into AI-augmented experts across the full audit cycle, data analysis automation, and intelligent documentation workflows.")
                .certificationDescription("Delivered by Nadia Yaich and Kais Khenine. Includes course support, coffee breaks, lunch, and AI tools used during training.")
                .brochureUrl("/pdfs/ai-audit.pdf")
                .intro("A practical certifying program designed for auditors, control teams, and risk professionals to operationalize generative AI in audit planning, execution, documentation, and assurance outcomes.")
                .participants("Administrators, Executives, Senior Managers, Department Heads, Risk Managers, Internal Controllers, Internal Auditors.")
                .duration("4 days + final test")
                .location("Tunis")
                .language("French")
                .learnPoints(List.of(
                    "Transform audit practices using AI-assisted analysis and documentation.",
                    "Master prompt engineering for control, audit, and risk use cases.",
                    "Use NotebookLM and Claude workflows for structured audit intelligence.",
                    "Automate risk diagnostics and support compliance verification.",
                    "Apply advanced methods for data reliability, interview augmentation, and dashboarding."
                ))
                .journeySteps(List.of(
                    JourneyStep.builder().title("Journey 01 - Explore AI Foundations").detail("Understand tools and prompt techniques for audit contexts.").build(),
                    JourneyStep.builder().title("Journey 02 - Build AI Workspaces").detail("Structure projects with NotebookLM and Claude collaborative workflows.").build(),
                    JourneyStep.builder().title("Journey 03 - Automate Controls and Risk Checks").detail("Deploy AI for diagnostics, conformity checks, and anomaly identification.").build(),
                    JourneyStep.builder().title("Journey 04 - Operationalize with Governance").detail("Finalize practical implementation plan, ethics guardrails, and certification test.").build()
                ))
                .build()
        };

        for (Course course : courses) {
            courseRepository.save(course);
        }
    }

    private void seedServicePages() {
        try {
            String json = loadJsonFromResource("seed/services.json");
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> pages = mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
            for (Map<String, Object> map : pages) {
                String contentJsonStr = mapper.writeValueAsString(map.get("contentJson"));
                ServicePage page = ServicePage.builder()
                        .slug((String) map.get("slug"))
                        .title((String) map.get("title"))
                        .subtitle((String) map.get("subtitle"))
                        .description((String) map.get("description"))
                        .layoutType((String) map.get("layoutType"))
                        .contentJson(contentJsonStr)
                        .build();
                servicePageRepository.save(page);
            }
        } catch (Exception e) {
            log.error("Failed to seed service pages: ", e);
        }
    }
}
