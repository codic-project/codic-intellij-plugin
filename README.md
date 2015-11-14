# Codic Intellij IDEA Plugin
IntelliJ IDEAとそのプラットフォーム製品 (PhpStorm, PyCharm, RubyMine, WebStorm)、[Codic](https://codic.jp) プラグインです。

![codic plugin](https://raw.githubusercontent.com/codic-project/codic-intellij-plugin/master/img/screenshot1.png)

### インストール
1. インストールは公式レポジトリからインストールできます。
 メニューの "Preferences" >> "Plugins" から "Codic Plugin" で検索してインストールしてください。

 ![codic plugin](https://raw.githubusercontent.com/codic-project/codic-intellij-plugin/master/img/screenshot2.png)

2. メニューの "Preferences" >> "Codic Plugin" でアクセストークンを設定します。アクセストークンは、
 [Codic](https://codic.jp)にログイン後、APIステータスのページより取得できます。
 
 ![codic plugin](https://raw.githubusercontent.com/codic-project/codic-intellij-plugin/master/img/screenshot3.png)

### 使い方

エディタ上で、<kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>D</kbd> でネーミング生成するためのポップアップを開きます。
テキストを選択状態で開くと、ダイレクト生成できます。

### 注意事項

このプラグインは、codic APIを使っているため、一定時間内のアクセス回数に制限があります。
制限を超えたら、APIステータスのページでリセットしてください。


### その他

バグ・要望などがありましたら、Issueへ登録してください。
