# Audit Log Browser

### What is Audit Log Browser?

Audit Log Browser は Alfresco Content Services の監査ログをブラウザの画面から閲覧することができるアドオンです。

### Description

Alfresco の監査ログは Alfresco のデータベースに格納されますが、ユーザフレンドリに見る方法がありません。Audit log Browser は Alfresco Content Services の管理コンソールに監査ログを表示する機能を追加し、ブラウザより確認することができます。
機能の詳細については **Specifications** の項をご覧ください。

### Installation

1. [Github のリリースページ](https://github.com/Ricksoft-OSS/audit-log-browser/releases)から Jar ファイルをダウンロードします
    1. audit-log-browser-platform-x.y.z.jar
    2. audit-log-browser-share-x.y.z.jar
2. Alfresco Content Services がインストールされているサーバに Jar ファイルを配置します
3. ＜Alfresco Content Services のインストールディレクトリ＞/module/platform ディレクトリ配下に audit-log-browser-platform-x.y.z.jar を配置します。ディレクトリがない場合は作成してから実行します
4. ＜Alfresco Content Services のインストールディレクトリ＞/module/share ディレクトリ配下に audit-log-browser-share-x.y.z.jar を配置します。ディレクトリがない場合は作成してから実行します
5. platform, share ディレクトリ、および 配置した Jar ファイルの所有者を Alfresco Content Services の実行ユーザに変更ます
6. Alfresco Content Services を再起動します

### Configuration

alfresco-global.properties で以下の設定を変更・追加することができます。デフォルトの値は表の通りです。

|設定内容|プロパティキー|デフォルト値|
|--------|--------------|------------|
|スケジュール機能の on/off            |AuditLogBrowser.schedule.enabled|true|
|スケジュール設定時の削除機能の on/off|AuditLogBrowser.schedule.delete.enabled|false|
|スケジュール処理の実行タイミング     |AuditLogBrowser.schedule.cron.expression|0 0 * * * ?|
|ACS インスタンスの起動からスケジューラの開始までの時間（ミリ秒）|AuditLogBrowser.schedule.cron.start.delay|240000|
|監査ログの保持期間（日）|AuditLogBrowser.schedule.archive.storage.period|7|

### Specifications

このアドオンでは以下の機能を利用することができます。

1. 一覧表示機能
2. 検索機能
3. 削除機能
4. スケジュール機能（スケジュールアーカイブ・スケジュール削除）
5. ダウンロード機能

取得できる監査ログの情報は次の項目です。

- ログイン・ログアウト・ログイン失敗
- コンテンツのプレビュー、ダウンロード（これらは同一のイベントとして表示されます）
- フォルダの作成と削除
- コンテンツのコピー、移動
- コンテンツのチェックイン・チェックアウト・チェックアウトのキャンセル
- フォルダのリンク
- プロパティの追加、更新
- アスペクトの追加、削除
- ユーザの作成、更新、削除
- コンテンツに対する権限の操作
- コンテンツ以外のものの権限の操作
- サイト権限の操作
- 文書オーナーの設定
- 変更後の文書タイプ
- ユーザの作成・削除・変更
- グループの作成・削除・変更

### Limitations

1. ご利用の際には Firefox または Google Chrome をご利用ください。
2. 一覧表示画面では、1度に表示される監査ログの件数は100件です。
3. 検索機能で、開始日が未設定の場合、開始時刻が設定されていても検索条件には使用されません（未設定の状態と見なされる）。
4. 検索機能で、終了日が未設定の場合、終了時刻が設定されていても検索条件には使用されません（未設定の状態と見なされる）。
5. 検索機能で、ユーザ・コンテンツの検索条件は完全一致での検索になります。
6. 削除機能で、開始日、終了日のいずれかが未設定の場合は削除できません。
7. スケジュール設定時の削除機能は、スケジュール機能がonになっている(有効化されている)ことが前提となります。スケジュール機能がoffの場合、スケジュール設定時の削除機能は常にoffになります。
8. スケジュール機能で、監査ログは記録されてから７日間を過ぎると、Zipファイルとして共有リポジトリ内に保存されます。その際にスケジュール設定時の削除機能がonに設定されている場合は、監査ログがデータベースから削除されます。
9. ダウンロード機能はログの量が多い場合には、サーバのパフォーマンス低下や、タイムアウトに繋がる可能性があります。監査ログをリポジトリに格納する機能を利用することで、圧縮された監査ログを１日ごとに分けてダウンロードすることができます。

### Contribution

バグ修正や機能追加などの依頼は Github の Issue を作成もしくは Pull Request をお願いします。

### Credit

- Shogo Yamaguchi (yamaguchi.shogo@ricksoft.jp)
- Yuuki Ebihara (ebihara.yuki@ricksoft.jp)

### License

Copyright 2018 Ricksoft Co., Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
