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
import bfc.bfc.entities.Partner;
import bfc.bfc.entities.ClientLogo;
import bfc.bfc.entities.JourneyStep;
import bfc.bfc.entities.ContactServiceOption;
import bfc.bfc.repository.PartnerRepository;
import bfc.bfc.repository.ClientLogoRepository;
import bfc.bfc.repositories.ContactServiceOptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

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
    private final PartnerRepository partnerRepository;
    private final ClientLogoRepository clientLogoRepository;
    private final ContactServiceOptionRepository contactServiceOptionRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.seed.frontend-assets:}")
    private String frontendAssetsOverride;

    public DataSeeder(UserRepository userRepository,
                      TeamMemberRepository teamMemberRepository, 
                      ArticleRepository articleRepository,
                      CourseRepository courseRepository,
                      ProjectRepository projectRepository,
                      HistoryEventRepository historyEventRepository,
                      RepresentativeRepository representativeRepository,
                      ServicePageRepository servicePageRepository,
                      PartnerRepository partnerRepository,
                      ClientLogoRepository clientLogoRepository,
                      ContactServiceOptionRepository contactServiceOptionRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.articleRepository = articleRepository;
        this.courseRepository = courseRepository;
        this.projectRepository = projectRepository;
        this.historyEventRepository = historyEventRepository;
        this.representativeRepository = representativeRepository;
        this.servicePageRepository = servicePageRepository;
        this.partnerRepository = partnerRepository;
        this.clientLogoRepository = clientLogoRepository;
        this.contactServiceOptionRepository = contactServiceOptionRepository;
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
            // Backfill roleTypes and countryFlagUrl on existing members
            List<TeamMember> allMembers = teamMemberRepository.findAll();
            boolean updated = false;
            for (TeamMember m : allMembers) {
                if (m.getRoleTypes() == null || m.getRoleTypes().isEmpty()) {
                    List<TeamMemberRole> roles = new ArrayList<>();
                    if (m.getRoleType() != null) {
                        roles.add(m.getRoleType());
                    }
                    m.setRoleTypes(roles);
                    updated = true;
                }
                if (m.getCountryFlagUrl() == null || m.getCountryFlagUrl().isBlank()) {
                    String flagUrl = resolveFlagUrl(m.getCountryName());
                    if (flagUrl != null) {
                        m.setCountryFlagUrl(flagUrl);
                        updated = true;
                    }
                }
                teamMemberRepository.save(m);
            }
            if (updated) log.info("Backfilled roleTypes and countryFlagUrl on existing team members");
            else log.info("Team members already have roleTypes and countryFlagUrl, skipping seed");
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

        // Seed service pages (only if empty); otherwise backfill empty content
        if (servicePageRepository.count() == 0) {
            seedServicePages();
            log.info("Service pages seeded successfully");
        } else {
            backfillEmptyServicePages();
            log.info("Service pages already exist, empty content backfilled if needed");
        }

        // Seed courses once
        if (courseRepository.count() == 0) {
            seedCourses();
            log.info("Courses seeded successfully");
        } else {
            log.info("Courses already exist, skipping seed");
        }

        // Seed history events once (do not wipe; reps may add their own events)
        if (historyEventRepository.count() == 0) {
            seedHistoryEvents();
            log.info("History events seeded successfully");
        } else {
            log.info("History events already exist, skipping seed");
        }

        // Seed representatives + flags once — never reseed
        if (representativeRepository.count() == 0) {
            seedRepresentatives();
            log.info("Representatives seeded successfully");
        } else {
            log.info("Representatives already exist, skipping seed");
        }

        // One-time photo seed from frontend src/assets (clients + partners)
        seedClientLogos();
        seedPartners();

        // One-time consulting service options for the contact form
        if (contactServiceOptionRepository.count() == 0) {
            seedContactServiceOptions();
            log.info("Contact service options seeded successfully");
        } else {
            log.info("Contact service options already exist, skipping seed");
        }
    }

    private void seedContactServiceOptions() {
        List<ContactServiceOption> options = List.of(
            ContactServiceOption.builder().value("Training").label("Training").displayOrder(1).build(),
            ContactServiceOption.builder().value("Consulting").label("Consulting").displayOrder(2).build(),
            ContactServiceOption.builder().value("Audit").label("Audit").displayOrder(3).build(),
            ContactServiceOption.builder().value("Tax and Legal").label("Tax and Legal").displayOrder(4).build(),
            ContactServiceOption.builder().value("Expertise").label("Expertise").displayOrder(5).build(),
            ContactServiceOption.builder().value("Collaboration").label("Collaboration").displayOrder(6).build(),
            ContactServiceOption.builder().value("Other").label("Other").displayOrder(7).build()
        );
        contactServiceOptionRepository.saveAll(options);
    }

    /**
     * One-time seed: copy global client logos from
     * bfc-consulting-innovation/src/assets/Logo references into uploads/clients
     * and insert client_logos rows (only when empty or logo files are missing).
     */
    private void seedClientLogos() {
        try {
            Path assets = resolveFrontendAssetsDir();
            if (assets == null) {
                log.warn("Frontend assets directory not found; skipping client logo photo seed");
                return;
            }

            Path sourceDir = assets.resolve("Logo references");
            Path destDir = Paths.get(uploadDir, "clients");
            if (!Files.isDirectory(sourceDir)) {
                log.warn("Client logo source not found: {}", sourceDir);
                return;
            }
            Files.createDirectories(destDir);

            Map<String, String> seeded = new LinkedHashMap<>(); // safeFilename -> public url
            try (Stream<Path> files = Files.list(sourceDir)) {
                files.filter(DataSeeder::isImageFile)
                     .sorted()
                     .forEach(file -> {
                         try {
                             String safeName = toSafeFilename(file.getFileName().toString());
                             Path dest = destDir.resolve(safeName);
                             if (!Files.exists(dest)) {
                                 Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
                             }
                             seeded.put(safeName, "/uploads/clients/" + safeName);
                         } catch (IOException e) {
                             log.warn("Failed to copy client logo {}: {}", file, e.getMessage());
                         }
                     });
            }

            if (seeded.isEmpty()) {
                log.warn("No client logo images found in {}", sourceDir);
                return;
            }

            boolean empty = clientLogoRepository.count() == 0;
            boolean broken = clientLogoRepository.findAll().stream()
                    .anyMatch(c -> !logoFileExists(c.getLogoUrl()));
            if (!empty && !broken) {
                log.info("Client logos already seeded and files present, skipping");
                return;
            }

            if (!empty) {
                clientLogoRepository.deleteAll();
                log.info("Client logo rows had missing photo files; re-seeding from src/assets");
            }

            List<ClientLogo> rows = new ArrayList<>();
            int order = 1;
            for (Map.Entry<String, String> entry : seeded.entrySet()) {
                rows.add(new ClientLogo(displayNameForClient(entry.getKey()), entry.getValue(), order++));
            }
            clientLogoRepository.saveAll(rows);
            log.info("Client logos seeded from assets: {}", rows.size());
        } catch (Exception e) {
            log.error("Client logo photo seed failed", e);
        }
    }

    /**
     * One-time seed: copy partner photos from src/assets/certif (+ reanda.png)
     * into uploads/certif and insert partners rows (only when empty or files missing).
     */
    private void seedPartners() {
        try {
            Path assets = resolveFrontendAssetsDir();
            if (assets == null) {
                log.warn("Frontend assets directory not found; skipping partner photo seed");
                return;
            }

            Path destDir = Paths.get(uploadDir, "certif");
            Files.createDirectories(destDir);

            Map<String, String> preferredNames = new LinkedHashMap<>();
            preferredNames.put("reanda.png", "Reanda international network");
            preferredNames.put("ici.png", "Internal Control Institute");
            preferredNames.put("irm.png", "Institute of Risk Management");
            preferredNames.put("global_innovation_insititute.png", "GINI");
            preferredNames.put("tabc.png", "Tunisia africa business council");

            // Preferred order first, then any remaining images from certif/
            List<Path> sources = new ArrayList<>();
            Path reanda = assets.resolve("reanda.png");
            if (Files.isRegularFile(reanda)) {
                sources.add(reanda);
            }
            Path certifDir = assets.resolve("certif");
            if (Files.isDirectory(certifDir)) {
                try (Stream<Path> files = Files.list(certifDir)) {
                    files.filter(DataSeeder::isImageFile)
                         .sorted()
                         .forEach(sources::add);
                }
            }

            Map<String, String> seeded = new LinkedHashMap<>(); // safeFilename -> public url
            for (Path source : sources) {
                try {
                    String safeName = toSafeFilename(source.getFileName().toString());
                    Path dest = destDir.resolve(safeName);
                    if (!Files.exists(dest)) {
                        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                    }
                    seeded.put(safeName, "/uploads/certif/" + safeName);
                } catch (IOException e) {
                    log.warn("Failed to copy partner photo {}: {}", source, e.getMessage());
                }
            }

            if (seeded.isEmpty()) {
                log.warn("No partner images found under {}", assets);
                return;
            }

            boolean empty = partnerRepository.count() == 0;
            boolean broken = partnerRepository.findAll().stream()
                    .allMatch(p -> !logoFileExists(p.getLogoUrl()));
            if (!empty && !broken) {
                log.info("Partners already seeded and photo files present, skipping");
                return;
            }

            if (!empty) {
                partnerRepository.deleteAll();
                log.info("Partner rows had missing photo files; re-seeding from src/assets");
            }

            List<Partner> rows = new ArrayList<>();
            int order = 1;
            for (Map.Entry<String, String> entry : seeded.entrySet()) {
                String display = preferredNames.getOrDefault(entry.getKey().toLowerCase(Locale.ROOT),
                        displayNameFromFilename(entry.getKey()));
                rows.add(new Partner(display, entry.getValue(), order++));
            }
            partnerRepository.saveAll(rows);
            log.info("Partners seeded from assets: {}", rows.size());
        } catch (Exception e) {
            log.error("Partner photo seed failed", e);
        }
    }

    private Path resolveFrontendAssetsDir() {
        List<Path> candidates = new ArrayList<>();
        if (frontendAssetsOverride != null && !frontendAssetsOverride.isBlank()) {
            candidates.add(Paths.get(frontendAssetsOverride));
        }
        candidates.add(Paths.get("..", "..", "bfc-consulting-innovation", "src", "assets"));
        candidates.add(Paths.get("..", "bfc-consulting-innovation", "src", "assets"));
        candidates.add(Paths.get("bfc-consulting-innovation", "src", "assets"));
        candidates.add(Paths.get("..", "..", "..", "bfc-consulting-innovation", "src", "assets"));

        for (Path candidate : candidates) {
            Path normalized = candidate.toAbsolutePath().normalize();
            if (Files.isDirectory(normalized)) {
                return normalized;
            }
        }
        return null;
    }

    private boolean logoFileExists(String logoUrl) {
        if (logoUrl == null || logoUrl.isBlank()) {
            return false;
        }
        String relative = logoUrl;
        if (relative.startsWith("/uploads/")) {
            relative = relative.substring("/uploads/".length());
        } else if (relative.startsWith("uploads/")) {
            relative = relative.substring("uploads/".length());
        } else if (relative.startsWith("/")) {
            relative = relative.substring(1);
        }
        if (relative.startsWith("http")) {
            return true;
        }
        return Files.exists(Paths.get(uploadDir, relative));
    }

    private static boolean isImageFile(Path path) {
        if (!Files.isRegularFile(path)) {
            return false;
        }
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".png") || name.endsWith(".jpg")
                || name.endsWith(".jpeg") || name.endsWith(".webp")
                || name.endsWith(".gif") || name.endsWith(".svg");
    }

    private static String toSafeFilename(String original) {
        int dot = original.lastIndexOf('.');
        String stem = dot >= 0 ? original.substring(0, dot) : original;
        String ext = dot >= 0 ? original.substring(dot).toLowerCase(Locale.ROOT) : "";
        stem = stem.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_")
                .replaceAll("_+", "_").replaceAll("^_|_$", "");
        return stem + ext;
    }

    private static String displayNameForClient(String safeFilename) {
        String stem = safeFilename.contains(".")
                ? safeFilename.substring(0, safeFilename.lastIndexOf('.'))
                : safeFilename;
        switch (stem) {
            case "world_bank": return "World Bank Group";
            case "giz_standard_logo_1": return "GIZ";
            case "expertise_france": return "Expertise France";
            case "uemoa": return "UEMOA";
            case "stb_bank": return "STB Bank";
            case "ooredoo_tunisie": return "Ooredoo";
            case "banque_atlantique": return "Banque Atlantique";
            case "amf_umoa": return "AMF UEMOA";
            default: return displayNameFromFilename(safeFilename);
        }
    }

    private static String displayNameFromFilename(String safeFilename) {
        String stem = safeFilename.contains(".")
                ? safeFilename.substring(0, safeFilename.lastIndexOf('.'))
                : safeFilename;
        String[] words = stem.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (words[i].isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            if (words[i].length() <= 3 && words[i].equals(words[i].toUpperCase(Locale.ROOT))) {
                sb.append(words[i]);
            } else {
                sb.append(Character.toUpperCase(words[i].charAt(0)));
                if (words[i].length() > 1) {
                    sb.append(words[i].substring(1));
                }
            }
        }
        return sb.length() > 0 ? sb.toString() : stem;
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

                HistoryEvent existing = historyEventRepository.findAll().stream()
                        .filter(e -> rep.getTitle() != null && rep.getTitle().equals(e.getTitle()))
                        .findFirst().orElse(null);
                if (existing != null) {
                    continue;
                }

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
                .roleTypes(List.of(TeamMemberRole.CEO, TeamMemberRole.MANAGING_PARTNER))
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
                .roleTypes(List.of(TeamMemberRole.ASSOCIATE))
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
                .roleTypes(List.of(TeamMemberRole.COUNTRY_MANAGER))
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
                .roleTypes(List.of(TeamMemberRole.COUNTRY_MANAGER))
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
                .roleTypes(List.of(TeamMemberRole.CONSULTANT))
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
                .roleTypes(List.of(TeamMemberRole.AUDITING_ACCOUNTANT))
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

    private void backfillEmptyServicePages() {
        try {
            String json = loadJsonFromResource("seed/services.json");
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> pages = mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
            for (Map<String, Object> map : pages) {
                String slug = (String) map.get("slug");
                ServicePage existing = servicePageRepository.findBySlug(slug).orElse(null);
                if (existing == null) {
                    String contentJsonStr = mapper.writeValueAsString(map.get("contentJson"));
                    ServicePage page = ServicePage.builder()
                            .slug(slug)
                            .title((String) map.get("title"))
                            .subtitle((String) map.get("subtitle"))
                            .description((String) map.get("description"))
                            .layoutType((String) map.get("layoutType"))
                            .contentJson(contentJsonStr)
                            .build();
                    servicePageRepository.save(page);
                    log.info("Backfilled missing service page: {}", slug);
                    continue;
                }
                if (isBlankServiceContent(existing.getContentJson())) {
                    existing.setContentJson(mapper.writeValueAsString(map.get("contentJson")));
                    if (existing.getTitle() == null || existing.getTitle().isBlank()) {
                        existing.setTitle((String) map.get("title"));
                    }
                    if (existing.getDescription() == null || existing.getDescription().isBlank()) {
                        existing.setDescription((String) map.get("description"));
                    }
                    if (existing.getLayoutType() == null || existing.getLayoutType().isBlank()) {
                        existing.setLayoutType((String) map.get("layoutType"));
                    }
                    servicePageRepository.save(existing);
                    log.info("Backfilled empty content for service page: {}", slug);
                }
            }
        } catch (Exception e) {
            log.error("Failed to backfill service pages: ", e);
        }
    }

    private static final java.util.Map<String, String> COUNTRY_FLAG_URLS = Map.of(
        "Republic of the Congo", "https://flagcdn.com/w80/cg.png",
        "Guinea", "https://flagcdn.com/w80/gn.png",
        "Senegal", "https://flagcdn.com/w80/sn.png",
        "Mauritania", "https://flagcdn.com/w80/mr.png",
        "Tunisia", "https://flagcdn.com/w80/tn.png"
    );

    private String resolveFlagUrl(String countryName) {
        if (countryName == null || countryName.isBlank()) return null;
        String url = COUNTRY_FLAG_URLS.get(countryName.trim());
        if (url != null) return url;
        String code = countryName.trim().toLowerCase().replaceAll("[^a-z]", "");
        if (code.length() < 2) return null;
        return "https://flagcdn.com/w80/" + code.substring(0, 2) + ".png";
    }

    private boolean isBlankServiceContent(String contentJson) {
        if (contentJson == null || contentJson.isBlank()) {
            return true;
        }
        String normalized = contentJson.replaceAll("\\s+", "");
        return normalized.isEmpty()
                || normalized.equals("null")
                || normalized.equals("{}")
                || normalized.equals("{\"categories\":[]}")
                || normalized.equals("{\"boxes\":[]}");
    }
}
