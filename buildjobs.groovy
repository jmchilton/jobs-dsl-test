@Grab(group='org.yaml', module='snakeyaml', version='1.13') 
import org.yaml.snakeyaml.Yaml


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
    def yaml = new Yaml()
    def recipeDef = yaml.load(defYamlString)
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

