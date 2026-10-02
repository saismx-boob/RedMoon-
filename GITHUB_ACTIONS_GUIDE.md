# Guide de Compilation Automatique sur GitHub Actions

Ce projet est entièrement préconfiguré pour la compilation continue (CI/CD) sur **GitHub Actions**. Dès que vous publiez ce code sur votre dépôt GitHub, vos APK et AAB (Android App Bundle) sont compilés, testés et téléchargeables automatiquement.

---

## 📁 Éléments ajoutés au projet

1. **`.github/workflows/android.yml`**
   - **Déclencheurs** : Se lance automatiquement à chaque `push` ou `pull_request` sur les branches `main` et `master`, ou manuellement via le bouton **« Run workflow »**.
   - **Tâches** :
     - Installation de Java JDK 17 (Temurin).
     - Mise en cache intelligente de Gradle pour des compilations rapides (1-2 minutes).
     - Décodage automatique du `debug.keystore` et du fichier `.env`.
     - Exécution de tous les tests unitaires et de combat (`testDebugUnitTest`).
     - Compilation de l'**APK Debug** (`app-debug.apk`).
     - Compilation du bundle **AAB Debug** pour les tests.
     - **Téléchargement automatique** des artefacts `.apk` directement depuis GitHub.

2. **`.github/workflows/release.yml`**
   - **Déclencheurs** : Lors de la création d'un tag de version (ex: `v1.0.0`) ou manuellement avec choix de version.
   - **Tâches** :
     - Compilation des versions Release (`assembleRelease` et `bundleRelease`).
     - Signature avec votre clé de production ou fallback automatique avec la clé de debug.
     - Création d'une **GitHub Release** officielle avec l'APK et l'AAB attachés en pièce jointe.

3. **Wrapper Gradle (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`)**
   - Indispensable pour permettre aux serveurs de GitHub Actions (Ubuntu) d'exécuter Gradle sans nécessiter d'installation préalable.

4. **`debug.keystore.base64`**
   - Version encodée du keystore de développement, versionnée dans Git pour garantir qu'aucune compilation sur GitHub ne s'arrête en erreur de signature.

---

## 🚀 Comment déployer sur votre compte GitHub

Si vous n'avez pas encore lié ce projet à GitHub, exécutez simplement les commandes suivantes depuis votre terminal local :

```bash
# 1. Initialiser le dépôt git local (si ce n'est pas déjà fait)
git init

# 2. Ajouter tous les fichiers du projet (y compris .github/)
git add .

# 3. Créer le commit initial
git commit -m "feat: Kairo 2D Fighting Game Engine avec CI/CD GitHub Actions"

# 4. Définir la branche principale
git branch -M main

# 5. Lier votre dépôt GitHub distant
git remote add origin https://github.com/VOTRE_PSEUDO/VOTRE_DEPOT.git

# 6. Pousser vers GitHub
git push -u origin main
```

---

## 📥 Où trouver et télécharger votre APK compilé ?

1. Rendez-vous sur votre dépôt GitHub dans votre navigateur.
2. Cliquez sur l'onglet **« Actions »** en haut de la page.
3. Cliquez sur le dernier workflow exécuté (ex : *Android CI - Build Kairo Fighter APK*).
4. En bas de la page de résumé du run, vous trouverez la section **Artifacts** :
   - Cliquez sur **`Kairo-Fighter-Debug-APK`** pour télécharger le fichier ZIP contenant votre fichier `.apk` prêt à être installé sur votre téléphone ou émulateur Android !

---

## 🔐 (Optionnel) Signature de Production pour le Google Play Store

Si vous souhaitez générer un APK ou un AAB signé pour le Google Play Store :
1. Dans GitHub, allez dans **Settings** de votre dépôt > **Secrets and variables** > **Actions**.
2. Ajoutez les secrets suivants :
   - `RELEASE_KEYSTORE_BASE64` : Le contenu en base64 de votre fichier `.jks` (obtenu via `base64 -w 0 mon-fichier.jks`).
   - `STORE_PASSWORD` : Le mot de passe de votre keystore.
   - `KEY_ALIAS` : L'alias de votre clé.
   - `KEY_PASSWORD` : Le mot de passe de la clé.
3. Lancez le workflow **« Build & Publish Release APK »** ou poussez un tag `git tag v1.0.0 && git push origin v1.0.0`. Le fichier signé sera automatiquement généré.
