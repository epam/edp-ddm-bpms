import com.epam.edp.stages.impl.ci.ProjectType
import com.epam.edp.stages.impl.ci.Stage

@Stage(name = "carrier-sast", buildTool = "any", type = [ProjectType.APPLICATION, ProjectType.LIBRARY])
class CarrierSast {
    Script script
    void run(context) {
        script.dir("${context.workDir}") {
            script.withCredentials([script.usernamePassword(credentialsId: "carrier-credentials",
                    passwordVariable: 'TOKEN', usernameVariable: 'USERNAME')]) {
                script.sh "sed 's/PROJECT-NAME/${context.codebase.name}/' /tmp/carrier-config/config.yaml > ./config.yaml"
                script.sh "dusty run -s sastJava -c ./config.yaml"
                script.archiveArtifacts artifacts: 'report.html, report.xml'
            }
        }
    }
}
return CarrierSast