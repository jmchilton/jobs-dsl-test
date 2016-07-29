// require(url:'http://jyaml.sourceforge.net', jar:'jyaml.jar', version:'1.0')
import org.ho.yaml.Yaml


def githubOrg = 'jmchilton'
def githubProject = 'jobs-dsl-test'
def baseJobName = 'community-tests-'

def engine = new groovy.text.SimpleTemplateEngine()

script_path = new File(__FILE__)
recipePath = new File(script_path.getParentFile(), 'recipes')

testShellTemplate = engine.createTemplate('make TEST_EXPRESSION="-k $testName"')


recipePath.eachFile {
    def recipeName = it.name
    def jobName = "${baseJobName}${recipeName}"
    def defYamlString = readFileFromWorkspace(new File(it, "def.yml"))
    def recipeDef = Yaml.load(defYamlString)
    templateBinding = [
        "testName": recipeName
    ]
    job(jobName) {
        description(recipeDef["description"])
        scm {
            git("git://github.com/${githubOrg}/${githubProject}.git")
        }
        steps {
            shell(testShellTemplate.make(templateBinding).toString())
        }
    }
}

