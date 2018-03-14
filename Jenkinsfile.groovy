final String mvn = "mvn --batch-mode --update-snapshots"

pipeline {
    agent any
    stages {
        stage('Environment') {
            steps {
                sh 'set'
            }
        }
        stage('no SNAPSHOT in master') {
            // checks that the pom version is not a snapshot when the current or target branch is master
            when {
                expression {
                    (env.GIT_BRANCH == 'master' || env.CHANGE_TARGET == 'master') &&
                            (readMavenPom(file: 'pom.xml').version).contains("SNAPSHOT")
                }
            }
            steps {
                error("Build failed because SNAPSHOT version")
            }
        }
        stage('Static Code Analysis') {
            when { expression { findFiles(glob: '**/src/main/java/**/*.java').length > 0 } }
            steps {
                withMaven(maven: 'maven', jdk: 'JDK LTS') {
                    sh "${mvn} compile"
                    sh "${mvn} checkstyle:checkstyle"
                    sh "${mvn} pmd:pmd"
                    pmd canComputeNew: false, defaultEncoding: '', healthy: '', pattern: '', unHealthy: ''
                }
            }
        }
        stage('SonarQube (develop only)') {
            when { expression { env.GIT_BRANCH == 'develop' && env.GIT_URL.startsWith('https://') } }
            steps {
                withSonarQubeEnv('sonarqube') {
                    withMaven(maven: 'maven', jdk: 'JDK LTS') {
                        sh "${mvn} org.sonarsource.scanner.maven:sonar-maven-plugin:3.4.0.905:sonar"
                    }
                }
            }
        }
        stage('Build Java Next') {
            when { expression { true == false } }
            steps {
                withMaven(maven: 'maven', jdk: 'JDK Next') {
                    sh "${mvn} clean install -Djava.version=9"
                }
            }
        }
        stage('Build Java LTS') {
            steps {
                withMaven(maven: 'maven', jdk: 'JDK LTS') {
                    sh "${mvn} clean install"
                }
            }
        }
        stage('Test Results') {
            when { expression { findFiles(glob: '**/target/surefire-reports/*.xml').length > 0 } }
            steps {
                junit '**/target/surefire-reports/*.xml'
                jacoco exclusionPattern: '**/*{Test|IT|Main|Application|Immutable}.class'
                withMaven(maven: 'maven', jdk: 'JDK LTS') {
                    sh "${mvn} com.gavinmogan:codacy-maven-plugin:coverage " +
                            "-DcoverageReportFile=target/site/jacoco/jacoco.xml " +
                            "-DprojectToken=`$JENKINS_HOME/codacy/token` " +
                            "-DapiToken=`$JENKINS_HOME/codacy/apitoken` " +
                            "-Dcommit=`git rev-parse HEAD`"
                }
            }
        }
        stage('Archiving') {
            when { expression { findFiles(glob: '**/target/*.jar').length > 0 } }
            steps {
                archiveArtifacts '**/target/*.jar'
            }
        }
        stage('Deploy') {
            when { expression { (env.GIT_BRANCH == 'master' && env.GIT_URL.startsWith('https://')) } }
            steps {
                withMaven(maven: 'maven', jdk: 'JDK LTS') {
                    sh "${mvn} deploy --activate-profiles release -DskipTests=true"
                }
            }
        }
    }
}