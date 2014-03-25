Sequel.migration do
  up do
    create_table(:authentications) do
      foreign_key :user_id, :users, :foreign_key_constraint_name => 'authentications_fkey_user_id'
      String :uid, :size => 200
      String :provider, :size => 200
      DateTime :created_at
      DateTime :updated_at

      index [:uid, :provider], :unique => true, :name => 'authentications_unique_uid_provider'
    end
  end

  down do
    drop_table(:authentications)
  end
end
