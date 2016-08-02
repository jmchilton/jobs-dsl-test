import groovy.json.JsonSlurper

def env = System.getenv()

def scriptPath = new File(__FILE__)
def rootPath = scriptPath.getParentFile().getParentFile()

def jsonSlurper = new JsonSlurper()


println "debug-3"
println env
def just_dockerfiles_config_path = env.get("JUST_DOCKERFILES_CONFIG") ?: new File(rootPath, "just-dockerfiles.json").getAbsolutePath()

println "debug-2"
def just_dockerfiles_config_file = new File(just_dockerfiles_config_path)
def just_dockerfiles_config = jsonSlurper.parseText(just_dockerfiles_config_file.getText())

println "debug-1"

def testGithubOrg = just_dockerfiles_config["testGithubOrg"]
def testGithubProject = just_dockerfiles_config.testGithubOrg.testGithubProject
def targetGithubOrg = just_dockerfiles_config.targetGithubOrg
def targetGithubProject = just_dockerfiles_config.targetGithubProject

def baseJobName = just_dockerfiles_config.baseJobName


def engine = new groovy.text.SimpleTemplateEngine()

println "debug0"

recipePath = new File(rootPath, 'recipes')
println "debug3"

testShellTemplate = engine.createTemplate('''
if [ ! -d ${testGithubProject} ];
then
    git clone --recursive git://github.com/${testGithubOrg}/${testGithubProject}.git
fi
cd ${testGithubProject};
git pull;
make run-test TARGET_ROOT=`pwd`/".." RECIPE_NAME="$testName" REPORT=`pwd`/../"report.xml"
''')

recipePath.eachFile {
    def recipeName = it.name
    def jobName = "${baseJobName}${recipeName}"
    println "tets here"
    def sout = new StringBuilder(), serr = new StringBuilder()
    def proc = "make dump-description RECIPE_NAME=${recipeName}".execute(null, rootPath)
    proc.consumeProcessOutput(sout, serr)
    proc.waitForOrKill(30000)
    println "out> $sout err> $serr"
    def defJsonString = new File(it, "def.json").text
    def recipeDef = jsonSlurper.parseText(defJsonString)
    templateBinding = [
        "testGithubOrg": testGithubOrg,
        "testGithubProject": testGithubProject,
        "testName": recipeName
    ]
    def shellCommand = "" // testShellTemplate.make(templateBinding).toString()
    def jenkinsDescription = """<p>${recipeDef["description"]}</p>
<p>This test can be executed locally as follows:</p>
<code>
${shellCommand}
</code>
    """
    job(jobName) {
        description(jenkinsDescription)
        scm {
            git("git://github.com/${targetGithubOrg}/${targetGithubProject}.git")
        }
        triggers {
            cron("@daily")
        }
        steps {
            shell(shellCommand)
        }
        publishers {
            archiveJunit 'report.xml'
        }
    }
}

    