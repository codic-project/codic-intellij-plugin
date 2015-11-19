# Codic Intellij IDEA Plugin
IntelliJ IDEAとそのプラットフォーム製品 (PhpStorm, PyCharm, RubyMine, WebStorm, ...)、[Codic](https://codic.jp) プラグインです。

![codic plugin](https://raw.githubusercontent.com/codic-project/codic-intellij-plugin/master/img/screenshot1.png)

### Install
1. インストールは公式レポジトリからインストールできます。
 メニューの "Preferences" >> "Plugins" から "Codic Plugin" で検索してインストールしてください。

 ![codic plugin](https://raw.githubusercontent.com/codic-project/codic-intellij-plugin/master/img/screenshot2.png)

2. メニューの "Preferences" >> "Codic Plugin" でアクセストークンを設定します。アクセストークンは、
 [Codic](https://codic.jp)にログイン後、APIステータスのページより取得できます。
 
 ![codic plugin](https://raw.githubusercontent.com/codic-project/codic-intellij-plugin/master/img/screenshot3.png)

### How to use

エディタ上で、<kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>D</kbd> でネーミング生成するためのポップアップを開きます。
テキストを選択状態で開くと、ダイレクト生成できます。

※ このプラグインは、codic APIを使っているため、一定時間内のアクセス回数に制限があります。
制限を超えたら、APIステータスのページでリセットしてください。

## Known issue

IntelliJのEditor popupが持つ既知の問題に起因して、Macことえりではポップアップ上でIME変換ができない現象があります。これについては、Issueを作成し調査しますが、問題が解決するまではテキスト選択+<kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>D</kbd>で生成するようにしてください。

### Bug report

バグ・要望などがありましたら、[Issue](https://github.com/codic-project/codic-intellij-plugin/issues)へ登録してください。
