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

### Change log

_1.0.11_
- IntelliJ 2016.1 で設定ダイアログが表示されない問題を修正

_1.0.10_
- APIレートリミットエラーを通知するように修正

_1.0.7_
- ケース変換選択のショートカットを追加（<kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>D</kbd>をリタイプ）

_1.0.6_
- API v1.1 をサポート.
- ダイアログ上でのキーハンドリングを強化

<!--
_1.0.5_
- バグ修正 #6 : Add vertical scrollbar in quick-look.

_1.0.4_
- Redesign the quick-look popup.
- バグ修正 #1 : IME dose not work in the quick-look.
-->


### Bug report

バグ・要望などがありましたら、[Issue](https://github.com/codic-project/codic-intellij-plugin/issues)へ登録してください。
